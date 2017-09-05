package com.github.downgoon.bsf.storage;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;

import com.github.downgoon.bsf.conf.BSFConf;
import com.github.downgoon.bsf.conf.BSFProtocol;
import com.github.downgoon.bsf.conf.BSFProtocolVer1;

/**
 * NOTE: non-thread-safe
 */
public class BSFSegment {

	/** bsf meta associated to this segment */
	private BSFMeta meta;

	/** segment number starting from 0 */
	private short number;

	/** segment file name */
	private String name;

	/** segment' head replicated in memory */
	private SegHead segHead = new SegHead();

	/** segment file */
	private RandomAccessFile randFile;

	/** segment contents in memory */
	private MappedByteBuffer fileBuf;

	private BSFProtocol bsfProto = new BSFProtocolVer1();

	private BSFConf bsfConf;

	BSFSegment(BSFMeta meta, short number, BSFConf bsfConf) {
		this.bsfConf = bsfConf;
		this.meta = meta;
		this.number = number;
		this.name = bsfProto.segmentFileName(meta.getName(), this.number);

	}

	public void open() throws IOException {
		File file = new File(this.name);
		final boolean isNewlyCreated = (!file.exists());

		// auto create file if not exists in 'rw' mode
		this.randFile = new RandomAccessFile(file, "rw");
		// space preallocation to segment file
		this.fileBuf = randFile.getChannel().map(MapMode.READ_WRITE, 0, bsfConf.getSegmentLimitBytes());

		if (isNewlyCreated) {
			segHead.save(); // create file and init head
		} else {
			segHead.load(); // load head from the file
		}

	}

	/**
	 * release segment into segment-object-pool
	 * 
	 * @throws IOException
	 *             if fail to close, throw {@link IOException}
	 */
	public void close() throws IOException {
		meta.releaseSegment(this.number);
	}

	/**
	 * realClose
	 */
	void realClose() throws IOException {
		fileBuf.force(); // sync to disk
		randFile.close();
		fileBuf = null; // release memory
	}

	/**
	 * append binary trunk data if the segment is not full after trunk appended
	 * 
	 * @param trunk
	 *            binary trunk data
	 * @return return false if segment will be full after appending, otherwise
	 *         return true
	 */
	public boolean appendTrunkIfNotFull(byte[] trunk) {
		if (!(trunk.length > 0 && trunk.length < bsfConf.getTrunkLimitBytes())) {
			throw new IllegalArgumentException(
					String.format("trunk limit violation: %sB", bsfConf.getTrunkLimitBytes()));
		}
		int wso = meta.getWriteSegmentOffset();
		if (!(wso + trunk.length + 4 < bsfConf.getSegmentLimitBytes())) {
			return false;
		}

		fileBuf.position(wso);
		fileBuf.putInt(trunk.length); // trunk head
		fileBuf.put(trunk); // trunk body

		segHead.incrTrunkCount();
		segHead.incrUsedSize(4 + trunk.length);

		meta.incrTrunkCount();
		meta.moveWriteSegmentOffset(4 + trunk.length); // including 4 bytes head
		return true;
	}

	/**
	 * deduct binary trunk data if the segment is not EOF before trunk deducted
	 * 
	 * @return return NULL if EOF, otherwise return trunk data
	 */
	public byte[] deductTrunkIfNotEOF() {
		int rso = meta.getReadSegmentOffset();
		if (rso >= segHead.usedSize) {
			return null; // indicating EOF
		}
		fileBuf.position(rso);
		int trunkSize = fileBuf.getInt();
		if (!(trunkSize > 0 && trunkSize <= bsfConf.getTrunkLimitBytes())) {
			throw new IllegalStateException(
					String.format("segment broken due to trunk head at RSO %s on %s", rso, name));
		}
		if (!(rso + 4 + trunkSize <= segHead.usedSize)) {
			throw new IllegalStateException(
					String.format("segment broken due to trunk body at ROS %s on %s", rso, name));
		}

		byte[] trunkData = new byte[trunkSize];
		fileBuf.get(trunkData); // dump data from buffer

		segHead.decrTrunkCount();

		meta.decrTrunkCount();
		meta.moveReadSegmentOffset(4 + trunkSize);
		return trunkData;
	}

	public short getNumber() {
		return number;
	}

	public String getName() {
		return name;
	}

	public BSFMeta getMeta() {
		return meta;
	}

	/**
	 * @return real used byte size of segment file excluding tail paddings
	 *         coming from file-memory-mapping's preallocation, but including
	 *         the heading consumed bytes of early trunks.
	 */
	public int getUsedSize() {
		return segHead.usedSize;
	}

	/**
	 * @return amount of trunks only stored in this segment and <b>not
	 *         consumed</b>.
	 */
	public int getTrunkCount() {
		return segHead.trunkCount;
	}

	@Override
	public String toString() {
		return "BSFSegment_" + number + " [TC=" + segHead.trunkCount + ", US=" + segHead.usedSize + "]";
	}

	/**
	 * 12B fixed head of segment file
	 */
	class SegHead {
		/**
		 * Protocol: meta file structure (0, 3B)
		 */
		private byte[] magic;

		/** version (3, 1B) */
		private byte version = BSFProtocol.VERSION;

		/**
		 * amount of trunks only stored in this segment and not consumed (4, 4B)
		 */
		private int trunkCount = 0;

		/**
		 * real used byte size of segment file excluding tail paddings coming
		 * from file-memory-mapping's preallocation, but including the heading
		 * consumed bytes of early trunks (8, 4B)
		 */
		private int usedSize = BSFProtocol.SEG_HEAD_SIZE;

		SegHead() {
			this.magic = new byte[BSFProtocol.MAGIC.length]; // deep copy
			System.arraycopy(BSFProtocol.MAGIC, 0, magic, 0, magic.length);
		}

		/** save into segment file head */
		void save() {
			fileBuf.position(0);
			fileBuf.put(this.magic);
			fileBuf.put(this.version);
			fileBuf.putInt(this.trunkCount);
			fileBuf.putInt(this.usedSize);
		}

		/** load from segment file */
		void load() {
			fileBuf.position(0);
			fileBuf.get(this.magic);
			this.version = fileBuf.get();
			this.trunkCount = fileBuf.getInt();
			this.usedSize = fileBuf.getInt();
		}

		public void incrTrunkCount() {
			this.trunkCount++;
			setTrunkCount(trunkCount);
		}

		public void decrTrunkCount() {
			this.trunkCount--;
			setTrunkCount(trunkCount);
		}

		public void incrUsedSize(int incrUS) {
			this.usedSize += incrUS;
			fileBuf.position(BSFProtocol.SEG_FP_US);
			fileBuf.putInt(this.usedSize);
		}

		public int getUsedSize() {
			return this.usedSize;
		}

		void setTrunkCount(int tc) {
			fileBuf.position(BSFProtocol.SEG_FP_TC);
			fileBuf.putInt(this.trunkCount);
		}

	}
}
