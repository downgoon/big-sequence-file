package com.github.downgoon.bsf.labs;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Files;

public class OneFileTwoMappedProcessA {

	public static void main(String[] args) throws Exception {
		File dir = Files.createTempDirectory("mapped").toFile();
		System.out.println("tmp dir: " + dir.getAbsolutePath());

		final String fileName = dir + File.separator + "one-file-two-mapped.data";
		
		Thread left4_thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					RandomAccessFile raf1 = new RandomAccessFile(new File(fileName), "rw");
					MappedByteBuffer mapBuf1 = raf1.getChannel().map(MapMode.READ_WRITE, 0, 8);
					
					
					// time1
					writeBuf(mapBuf1, 0, "aaaa");
					
					System.out.println("同步点1 @A"); // DEBUG模式设置断点
					String left4_mb1 = readBuf(mapBuf1, 0);
					String right4_mb1 = readBuf(mapBuf1, 4);
					System.out.println(String.format("left4_mb1: %s, right4_mb1: %s", left4_mb1, right4_mb1));
					
					// time2
					writeBuf(mapBuf1, 0, "cccc"); 
					
					System.out.println("同步点2 @A"); // DEBUG模式设置断点
					left4_mb1 = readBuf(mapBuf1, 0);
					right4_mb1 = readBuf(mapBuf1, 4);
					System.out.println(String.format("left4_mb1: %s, left4_mb1: %s", left4_mb1, right4_mb1));
					
					raf1.close();
					
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				
				
			}
			
		}, "left4_thread");
		
		
		left4_thread.start();
		
		
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
