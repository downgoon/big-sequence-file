package com.github.downgoon.bsf.labs;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Files;

import junit.framework.Assert;

public class OneFileTwoMapped {

	public static void main(String[] args) throws Exception {
		File dir = Files.createTempDirectory("mapped").toFile();
		System.out.println("tmp dir: " + dir.getAbsolutePath());

		String fileName = dir + File.separator + "one-file-two-mapped.data";

		RandomAccessFile raf1 = new RandomAccessFile(new File(fileName), "rw");
		RandomAccessFile raf2 = new RandomAccessFile(new File(fileName), "rw");

		MappedByteBuffer mapBuf1 = raf1.getChannel().map(MapMode.READ_WRITE, 0, 8);
		MappedByteBuffer mapBuf2 = raf2.getChannel().map(MapMode.READ_WRITE, 0, 8);
		
			
		System.out.println("(mapBuf1 == mapBuf2): " + (mapBuf1 == mapBuf2));	

		writeBuf(mapBuf1, 0, "aaaa");
		writeBuf(mapBuf2, 4, "bbbb");

		String left4_mb1 = readBuf(mapBuf1, 0);
		String left4_mb2 = readBuf(mapBuf2, 0);
		System.out.println(String.format("left4_mb1: %s, left4_mb2: %s", left4_mb1, left4_mb2));
		Assert.assertEquals(left4_mb1, left4_mb2);
		
		String right4_mb1 = readBuf(mapBuf1, 4);
		String right4_mb2 = readBuf(mapBuf2, 4);
		System.out.println(String.format("right4_mb1: %s, right4_mb2: %s", right4_mb1, right4_mb2));
		Assert.assertEquals(right4_mb1, right4_mb2);
		
		
		writeBuf(mapBuf1, 0, "cccc");
		writeBuf(mapBuf2, 4, "dddd");
		
		left4_mb1 = readBuf(mapBuf1, 0);
		left4_mb2 = readBuf(mapBuf2, 0);
		System.out.println(String.format("left4_mb1: %s, left4_mb2: %s", left4_mb1, left4_mb2));
		Assert.assertEquals(left4_mb1, left4_mb2);
		
		right4_mb1 = readBuf(mapBuf1, 4);
		right4_mb2 = readBuf(mapBuf2, 4);
		System.out.println(String.format("right4_mb1: %s, right4_mb2: %s", right4_mb1, right4_mb2));
		Assert.assertEquals(right4_mb1, right4_mb2);
		
		raf1.close();
		raf2.close();
	}

	static String readBuf(MappedByteBuffer buf, int offset) {
		byte[] b4 = new byte[4];
		buf.position(offset);
		buf.get(b4);
		return new String(b4);
	}

	static void writeBuf(MappedByteBuffer buf, int offset, String text) {
		buf.position(offset);
		buf.put(text.getBytes());
	}

}
