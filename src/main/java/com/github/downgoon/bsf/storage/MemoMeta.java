package com.github.downgoon.bsf.storage;

import java.io.IOException;

import com.github.downgoon.bsf.conf.BSFProtocol;

class MemoMeta implements Meta {

	/**
	 * Protocol: meta file structure (0, 3B)
	 */
	private byte[] magic;

	/** version (3, 1B) */
	private byte version = BSFProtocol.VERSION;

	/** total amount of binary trunks stored in all segments (4, 4B) */
	private int trunkCount = 0;

	/** WSN: Write Segment Number (8, 2B) */
	private short writeSegmentNumber = BSFProtocol.SEG_NUMBER_INIT;

	/** RSN: Read Segment Number (10, 2B) */
	private short readSegmentNumber = BSFProtocol.SEG_NUMBER_INIT;

	/** WSO: Write Segment Offset (12, 4B) */
	private int writeSegmentOffset = BSFProtocol.SEG_HEAD_SIZE;

	/** RSO: Read Segment Offset (16, 4B) */
	private int readSegmentOffset = BSFProtocol.SEG_HEAD_SIZE;

	/** (20, 4B) */
	private byte[] reserved;

	MemoMeta() {
		this.magic = new byte[BSFProtocol.MAGIC.length]; // deep copy
		System.arraycopy(BSFProtocol.MAGIC, 0, magic, 0, magic.length);
		this.reserved = new byte[BSFProtocol.RESERVED.length]; // deep copy
		System.arraycopy(BSFProtocol.RESERVED, 0, reserved, 0, reserved.length);
	}

	@Override
	public byte[] getMagic() {
		return this.magic;
	}

	@Override
	public byte getVersion() {
		return this.version;
	}

	@Override
	public int getTrunkCount() {
		return this.trunkCount;
	}

	@Override
	public short getWriteSegmentNumber() {
		return this.writeSegmentNumber;
	}

	@Override
	public short getReadSegmentNumber() {
		return this.readSegmentNumber;
	}

	@Override
	public int getWriteSegmentOffset() {
		return this.writeSegmentOffset;
	}

	@Override
	public int getReadSegmentOffset() {
		return this.readSegmentOffset;
	}

	@Override
	public byte[] getReserved() {
		return this.reserved;
	}

	/* writing operations */

	void setMagic(byte[] magic) {
		this.magic = magic;
	}

	void setVersion(byte version) {
		this.version = version;
	}

	void setTrunkCount(int trunkCount) {
		this.trunkCount = trunkCount;
	}

	@Override
	public int incrTrunkCount() {
		this.trunkCount++;
		return this.trunkCount;
	}

	@Override
	public int decrTrunkCount() {
		this.trunkCount--;
		return this.trunkCount;
	}

	@Override
	public void setWriteSegmentNumber(short writeSegmentNumber) {
		this.writeSegmentNumber = writeSegmentNumber;
	}

	@Override
	public void setReadSegmentNumber(short readSegmentNumber) {
		this.readSegmentNumber = readSegmentNumber;
	}

	@Override
	public void setWriteSegmentOffset(int writeSegmentOffset) {
		this.writeSegmentOffset = writeSegmentOffset;
	}

	@Override
	public void setReadSegmentOffset(int readSegmentOffset) {
		this.readSegmentOffset = readSegmentOffset;
	}

	void setReserved(byte[] reserved) {
		this.reserved = reserved;
	}
	

	@Override
	public void open() throws IOException {
		// do nothing
	}

	@Override
	public void close() throws IOException {
		// do nothing
	}

	@Override
	public String toString() {
		return "MemoMeta [TC=" + trunkCount + ", WSN=" + writeSegmentNumber
				+ ", RSN=" + readSegmentNumber + ", WSO=" + writeSegmentOffset
				+ ", RSO=" + readSegmentOffset + "]";
	}
	
	
}
