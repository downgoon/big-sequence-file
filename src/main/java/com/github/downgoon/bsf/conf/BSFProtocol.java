package com.github.downgoon.bsf.conf;

public interface BSFProtocol {

	/**
	 * magic field: BigFile Pipe (3.14 is Math.PI)
	 * */
	final byte[] MAGIC = new byte[] {(byte)0xBF, (byte)0x3F, (byte)0x14 };
	
	final byte VERSION = (byte)0x10;
	
	final byte[] RESERVED = new byte[] {(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF };
	

	
	/* about meta */
	
	final String META_FILE_EXT = ".bsf";
	
	/** the size of '.bsf' meta file's header */
	final int META_HEAD_SIZE = 24;
	
	/** meta field position for TC,WSN,RSN,WSO,RSO */
	final int META_FP_TC = 4;
	final int META_FP_WSN = 8;
	final int META_FP_RSN = 10;
	final int META_FP_WSO = 12;
	final int META_FP_RSO = 16;
	
	
	
	/* about segments */
	
	final String SEG_FILE_EXT = ".seg";
	
	/** the size of '.seg' segment file's header */
	final int SEG_HEAD_SIZE = 12;
	
	/** segment files's number starts from ZERO */
	final short SEG_NUMBER_INIT = 0;
	
	/** segment head field position for TC, US */
	final int SEG_FP_TC = 4; // TrunkCount
	final int SEG_FP_US = 8; // UsedSize
	
	
	/* some rules */
	
	public String metaFileName(String bsfName);
	
	public String segmentFileName(String bsfName, short segmentNumber);
	
}
