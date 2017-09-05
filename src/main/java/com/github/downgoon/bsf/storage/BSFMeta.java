package com.github.downgoon.bsf.storage;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.util.HashMap;
import java.util.Map;

import com.github.downgoon.bsf.conf.BSFConf;
import com.github.downgoon.bsf.conf.BSFProtocol;
import com.github.downgoon.bsf.conf.BSFProtocolVer1;

/**
 * NOTE: non-thread-safe
 */
public class BSFMeta implements Meta {

	/** meta in memory */
	private MemoMeta memoMeta;

	/**
	 * meta in file (Memory Mapping File)
	 */
	private RandomAccessFile randFile;
	private MappedByteBuffer fileMeta;

	private BSFProtocolVer1 protocolVer1 = new BSFProtocolVer1();

	/**
	 * bsf name
	 */
	private String name;

	private BSFConf bsfConf;

	/**
	 * @param	bsfName	full name of '.bsf' meta file
	 * @param	bsfConf	configuration items
	 */
	public BSFMeta(String bsfName, BSFConf bsfConf) {
		this.bsfConf = bsfConf;
		this.name = protocolVer1.metaFileName(bsfName);
	}

	public BSFMeta(String bsfName) {
		this(bsfName, BSFConf.conf());
	}

	@Override
	public void open() throws IOException {
		File file = new File(this.name);
		if (!file.exists()) {
			create(file);
		} else {
			load(file);
		}
	}

	@Override
	public void close() throws IOException {
		if (fileMeta != null) {
			fileMeta.force(); // flush to disk
			fileMeta = null; // release MemoryMapping
		}
		if (randFile != null) {
			randFile.close();
		}
	}

	/* reading operations */

	@Override
	public byte[] getMagic() {
		return memoMeta.getMagic();
	}

	@Override
	public byte getVersion() {
		return memoMeta.getVersion();
	}

	@Override
	public int getTrunkCount() {
		return memoMeta.getTrunkCount();
	}

	@Override
	public short getWriteSegmentNumber() {
		return memoMeta.getWriteSegmentNumber();
	}

	@Override
	public short getReadSegmentNumber() {
		return memoMeta.getReadSegmentNumber();
	}

	@Override
	public int getWriteSegmentOffset() {
		return memoMeta.getWriteSegmentOffset();
	}

	@Override
	public int getReadSegmentOffset() {
		return memoMeta.getReadSegmentOffset();
	}

	@Override
	public byte[] getReserved() {
		return memoMeta.getReserved();
	}

	public String getName() {
		return this.name;
	}

	/* writing operations */

	@Override
	public int incrTrunkCount() {
		return setTrunkCount0(memoMeta.incrTrunkCount());
	}

	@Override
	public int decrTrunkCount() {
		return setTrunkCount0(memoMeta.decrTrunkCount());
	}

	int setTrunkCount(int trunkCount) {
		memoMeta.setTrunkCount(trunkCount);
		return setTrunkCount0(trunkCount);
	}

	/**
	 * only set TrunkCount on File
	 */
	private int setTrunkCount0(int trunkCount) {
		fileMeta.position(BSFProtocol.META_FP_TC);
		fileMeta.putInt(trunkCount);
		return trunkCount;
	}

	@Override
	public void setWriteSegmentNumber(short writeSegmentNumber) {
		memoMeta.setWriteSegmentNumber(writeSegmentNumber);
		fileMeta.position(BSFProtocol.META_FP_WSN);
		fileMeta.putShort(writeSegmentNumber);
	}

	public void moveWriteSegmentNumber(int incrWSN) {
		setWriteSegmentNumber((short) (memoMeta.getWriteSegmentNumber() + incrWSN));
	}

	@Override
	public void setReadSegmentNumber(short readSegmentNumber) {
		memoMeta.setReadSegmentNumber(readSegmentNumber);
		fileMeta.position(BSFProtocol.META_FP_RSN);
		fileMeta.putShort(readSegmentNumber);
	}

	public void moveReadSegmentNumber(short incrRSN) {
		setReadSegmentNumber((short) (memoMeta.getReadSegmentNumber() + incrRSN));
	}

	@Override
	public void setWriteSegmentOffset(int writeSegmentOffset) {
		memoMeta.setWriteSegmentOffset(writeSegmentOffset);
		fileMeta.position(BSFProtocol.META_FP_WSO);
		fileMeta.putInt(writeSegmentOffset);
	}

	public void moveWriteSegmentOffset(int incrWSO) {
		setWriteSegmentOffset(memoMeta.getWriteSegmentOffset() + incrWSO);
	}

	@Override
	public void setReadSegmentOffset(int readSegmentOffset) {
		memoMeta.setReadSegmentOffset(readSegmentOffset);
		fileMeta.position(BSFProtocol.META_FP_RSO);
		fileMeta.putInt(readSegmentOffset);
	}

	public void moveReadSegmentOffset(int incrRSO) {
		setReadSegmentOffset(memoMeta.getReadSegmentOffset() + incrRSO);
	}

	/**
	 * create a new meta file and init its values
	 * 
	 * @param file
	 *            file to be created
	 * @throws IOException
	 *             if fail to create, throw IOException
	 */
	protected void create(File file) throws IOException {
		// auto create a newly file
		this.randFile = new RandomAccessFile(file, "rw");
		this.fileMeta = randFile.getChannel().map(MapMode.READ_WRITE, 0, BSFProtocol.META_HEAD_SIZE);
		this.memoMeta = new MemoMeta();
		this.fileMeta.put(memoMeta.getMagic());
		this.fileMeta.put(memoMeta.getVersion());
		this.fileMeta.putInt(memoMeta.getTrunkCount());
		this.fileMeta.putShort(memoMeta.getWriteSegmentNumber());
		this.fileMeta.putShort(memoMeta.getReadSegmentNumber());
		this.fileMeta.putInt(memoMeta.getWriteSegmentOffset());
		this.fileMeta.putInt(memoMeta.getReadSegmentOffset());
		this.fileMeta.put(memoMeta.getReserved());
	}

	/**
	 * load meta file
	 * 
	 * @param file
	 *            file to be loaded
	 * @throws IOException
	 *             if fail to load, throw IOException
	 */
	protected void load(File file) throws IOException {
		this.randFile = new RandomAccessFile(file, "rw");
		this.fileMeta = randFile.getChannel().map(MapMode.READ_WRITE, 0, BSFProtocol.META_HEAD_SIZE);
		this.memoMeta = new MemoMeta();

		fileMeta.get(memoMeta.getMagic()); // write NOT. read
		memoMeta.setVersion(fileMeta.get());
		memoMeta.setTrunkCount(fileMeta.getInt());
		memoMeta.setWriteSegmentNumber(fileMeta.getShort());
		memoMeta.setReadSegmentNumber(fileMeta.getShort());

		memoMeta.setWriteSegmentOffset(fileMeta.getInt());
		memoMeta.setReadSegmentOffset(fileMeta.getInt());

		fileMeta.get(memoMeta.getReserved()); // write NOT. read
	}

	@Override
	public String toString() {
		return memoMeta.toString();
	}

	private SegmentPool segPool = new SegmentPool(this);

	/**
	 * create a new segment or reuse an existing segment from the pool
	 * 
	 * @param number
	 *            segment number starting from 0
	 * 
	 * @return segment file object for specified number
	 * 
	 * @throws IOException
	 *             if fail to create or load segment file, throw IOException
	 */
	public BSFSegment openSegment(short number) throws IOException {
		return segPool.allocateSegment(number);
	}

	void releaseSegment(short number) throws IOException {
		segPool.releaseSegment(number);
	}

	/**
	 * object pool for segments
	 */
	private static class SegmentPool {

		private BSFMeta meta;

		public SegmentPool(BSFMeta meta) {
			this.meta = meta;
		}

		/**
		 * referring count for segments
		 */
		private Map<Short, Integer> segRefCount = new HashMap<Short, Integer>();

		/**
		 * object pool for segments: mapping from segment number to object
		 */
		private Map<Short, BSFSegment> segObjPool = new HashMap<Short, BSFSegment>();

		public BSFSegment allocateSegment(short number) throws IOException {

			BSFSegment segCached = segObjPool.get(number);
			if (segCached != null) {
				segRefCount.put(number, segRefCount.get(number) + 1);
				return segCached;
			}
			BSFSegment segCreated = new BSFSegment(meta, number, meta.bsfConf);
			segCreated.open();
			segObjPool.put(number, segCreated);
			segRefCount.put(number, 1);
			return segCreated;

		}

		public void releaseSegment(short number) throws IOException {
			Integer refCount = segRefCount.get(number);
			if (refCount == null) {
				return; // do nothing for not-existing segment
			}

			if (refCount > 1) {
				segRefCount.put(number, refCount - 1);
				return; // referring counting down
			}

			segRefCount.remove(number);
			BSFSegment segCached = segObjPool.remove(number);
			segCached.realClose();

		}
	}

}
