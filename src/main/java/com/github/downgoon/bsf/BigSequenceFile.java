package com.github.downgoon.bsf;

import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.github.downgoon.bsf.conf.BSFConf;
import com.github.downgoon.bsf.conf.BSFProtocol;
import com.github.downgoon.bsf.scheduler.SegmentCleaner;
import com.github.downgoon.bsf.storage.BSFMeta;
import com.github.downgoon.bsf.storage.BSFSegment;

/**
 * 
 * a <b>big</b> file providing two <b>sequential</b> access methods named
 * {@link #appendTrunk(byte[])} and {@link #deductTrunk()}.
 * 
 * <p>
 * in fact, the big file is not a single file, but consisting of one '.bsf' meta
 * file and one or many '.seg' segment files in its underlying layers.
 * </p>
 * 
 * <p>
 * <b>thread-safe</b> but mutual exclusive between writing
 * {@link #appendTrunk(byte[])} and reading {@link #deductTrunk()} operations.
 * </p>
 */
public class BigSequenceFile {

	private BSFMeta meta;

	/**
	 * writing segment: equal to {@link #rseg} only when they are the same
	 * number segments
	 */
	private BSFSegment wseg;

	/**
	 * reading segment: equal to {@link #wseg} only when they are the same
	 * number segments
	 */
	private BSFSegment rseg;

	private String name;

	private BSFConf bsfConf;

	private Semaphore writePermit;

	private Semaphore readPermit;

	/**
	 * exclusive lock between writing and reading
	 */
	private Lock lock = new ReentrantLock();

	private volatile boolean opened = false;

	private Lock lockOpenClose = new ReentrantLock();

	/**
	 * Async Segment Cleaner
	 */
	private static final SegmentCleaner segmentCleaner = new SegmentCleaner();

	public BigSequenceFile(String name) {
		this(name, BSFConf.conf());
	}

	public BigSequenceFile(String name, BSFConf bsfConf) {
		this.name = name;
		this.bsfConf = bsfConf;
	}

	public void open() throws IOException {
		while (!opened) {
			try {
				lockOpenClose.lock();
				if (!opened) {
					meta = new BSFMeta(name, bsfConf);
					meta.open();

					// open the last segment
					wseg = meta.openSegment(meta.getWriteSegmentNumber());
					rseg = meta.openSegment(meta.getReadSegmentNumber());

					// load permits from file
					writePermit = new Semaphore(bsfConf.getTrunkLimitCount() - meta.getTrunkCount());
					readPermit = new Semaphore(meta.getTrunkCount());
					opened = true;
				}

			} finally {
				lockOpenClose.unlock();
			}

		}
	}

	public void close() throws IOException {
		checkStatus();
		try {
			lockOpenClose.lock();
			if (opened) {
				wseg.close();
				rseg.close();
				meta.close();
				opened = false;
			}

		} finally {
			lockOpenClose.unlock();
		}

	}

	public boolean isOpen() {
		return opened;
	}

	/**
	 * append one trunk at the end.
	 * <p>
	 * the caller thread will be blocked if {@link #getTrunkCount()} exceeds
	 * {@link BSFConf#getTrunkLimitCount()}
	 * </p>
	 * 
	 * @param trunk
	 *            binary trunk data
	 */
	public void appendTrunk(byte[] trunk) {
		checkStatus();
		try {
			writePermit.acquire();
		} catch (InterruptedException e) {
			throw new IllegalStateException("write permit interrupted", e);
		}

		try {
			lock.lock(); // excluding other writing and reading

			boolean isNotFull = wseg.appendTrunkIfNotFull(trunk);
			if (isNotFull) {
				readPermit.release();
			}
			if (!isNotFull) { // moving to next segment
				BSFSegment wsegNext = null;
				short wsegNextNum = (short) (wseg.getNumber() + 1);
				try {
					wsegNext = meta.openSegment(wsegNextNum);
				} catch (IOException e) {
					throw new IllegalStateException("can't rotate at writing segment " + wseg.getName());
				}

				try {
					wseg.close();
				} catch (IOException e) {
					throw new IllegalStateException("can't close at writing segment " + wseg.getName());
				}

				wseg = wsegNext;
				meta.setWriteSegmentNumber(wsegNextNum);
				meta.setWriteSegmentOffset(BSFProtocol.SEG_HEAD_SIZE);
				wseg.appendTrunkIfNotFull(trunk);
				readPermit.release();
			}

		} finally {
			lock.unlock();
		}

	}

	/**
	 * read and remove one piece of trunk at the beginning.
	 * 
	 * @return the caller thread will be blocked if all trunks have been
	 *         consumed and woken up by another more appending.
	 */
	public byte[] deductTrunk() {
		checkStatus();
		try {
			readPermit.acquire(); // blocking on no more segments
		} catch (InterruptedException e) {
			throw new IllegalStateException("read permit interrupted", e);
		}

		try {
			lock.lock(); // excluding other writing and reading

			byte[] trunk = rseg.deductTrunkIfNotEOF();
			if (trunk != null) {
				writePermit.release();
				return trunk;
			}

			if (meta.getTrunkCount() <= 0) {
				throw new IllegalStateException("reading on end of segment, but no more segments before blocking");
			}

			// end of segment & moving onto next segment
			BSFSegment rsegNext = null;
			short rsegNextNum = (short) (rseg.getNumber() + 1);
			try {
				rsegNext = meta.openSegment(rsegNextNum);
			} catch (IOException e) {
				throw new IllegalStateException("can't rotate at reading segment " + rseg.getName());
			}

			try {
				rseg.close();
			} catch (IOException e) {
				throw new IllegalStateException("can't close at reading segment " + rseg.getName());
			}

			rseg = rsegNext;
			meta.setReadSegmentNumber(rsegNextNum);
			meta.setReadSegmentOffset(BSFProtocol.SEG_HEAD_SIZE);
			trunk = rseg.deductTrunkIfNotEOF();
			writePermit.release();

			// clean segment file in async way
			segmentCleaner.cleanSegment(rseg.getName());
			return trunk;

		} finally {
			lock.unlock();
		}

	}

	protected void checkStatus() {
		if (!opened) {
			throw new IllegalStateException("BigSequenceFile Not Open: " + name);
		}
	}

	/**
	 * @return total amount of trunks appended and not yet consumed
	 */
	public int getTrunkCount() {
		checkStatus();
		return meta.getTrunkCount();
	}

	/**
	 * @return full name of the file (strictly speaking meta file with '.bsf'
	 *         extension ).
	 */
	public String getName() {
		return meta.getName();
	}

	public String debugInfo() {
		return "meta: " + meta + ", wseg: " + wseg + ", rseg: " + rseg;
	}

	@Override
	public String toString() {
		return debugInfo();
	}

}
