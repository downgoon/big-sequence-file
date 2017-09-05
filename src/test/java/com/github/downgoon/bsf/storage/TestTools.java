package com.github.downgoon.bsf.storage;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import junit.framework.Assert;

public class TestTools {

	private boolean ennable = true;

	public TestTools(boolean ennable) {
		super();
		this.ennable = ennable;
	}

	/**
	 * Delete a non-empty directory: Deletes all files and subdirectories under
	 * dir.
	 */
	public boolean deleteDirRecur(File dir) {
		if (!ennable) {
			return false;
		}
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDirRecur(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}
		return dir.delete();
	}

	private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

	public static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

	/**
	 * @param expectedHex
	 *            expected file content in Hex format
	 */
	public static void checkFileContent(String expectedHex, String fileName, int checkLength)
			throws FileNotFoundException, IOException {
		DataInputStream dis = null;
		try {
			dis = new DataInputStream(new FileInputStream(new File(fileName)));
			byte[] fileBytes = new byte[checkLength];
			dis.readFully(fileBytes);
			String fileHex = TestTools.bytesToHex(fileBytes);
			Assert.assertEquals(expectedHex.toUpperCase(), fileHex.toUpperCase());

		} finally {
			if (dis != null) {
				dis.close();
			}
		}
	}

}
