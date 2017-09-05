package com.github.downgoon.bsf.storage;

import java.io.IOException;

interface Meta {

	byte[] getMagic();

	byte getVersion();

	int getTrunkCount();

	short getWriteSegmentNumber();

	short getReadSegmentNumber();

	int getWriteSegmentOffset();

	int getReadSegmentOffset();

	byte[] getReserved();

	int incrTrunkCount();

	int decrTrunkCount();

	void setWriteSegmentNumber(short writeSegmentNumber);

	void setReadSegmentNumber(short readSegmentNumber);

	void setWriteSegmentOffset(int writeSegmentOffset);

	void setReadSegmentOffset(int readSegmentOffset);

	void open() throws IOException;
	
	void close() throws IOException;

}
