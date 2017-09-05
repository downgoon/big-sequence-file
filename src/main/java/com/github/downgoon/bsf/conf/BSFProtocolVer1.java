package com.github.downgoon.bsf.conf;

/** BSF Protocol Version 1 */
public class BSFProtocolVer1 implements BSFProtocol {

	@Override
	public String metaFileName(String bsfName) {
		if (bsfName.endsWith(META_FILE_EXT)) {
			return bsfName;
		} else {
			return bsfName + META_FILE_EXT;
		}
	}

	@Override
	public String segmentFileName(String bsfName, short segmentNumber) {
		return trimExtension(bsfName) + "_" + segmentNumber + SEG_FILE_EXT;
	}

	private String trimExtension(String bsfName) {
		if (bsfName.endsWith(META_FILE_EXT)) {
			return bsfName.substring(0, bsfName.length() - META_FILE_EXT.length());
		}  else {
			return bsfName;
		}
	}
	
}
