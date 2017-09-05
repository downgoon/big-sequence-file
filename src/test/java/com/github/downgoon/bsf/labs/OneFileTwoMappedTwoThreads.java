package com.github.downgoon.bsf.labs;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Files;
import java.util.concurrent.CountDownLatch;

public class OneFileTwoMappedTwoThreads {

	public static void main(String[] args) throws Exception {
		File dir = Files.createTempDirectory("mapped").toFile();
		System.out.println("tmp dir: " + dir.getAbsolutePath());

		final String fileName = dir + File.separator + "one-file-two-mapped.data";
		
		final CountDownLatch join_start = new CountDownLatch(1);
		final CountDownLatch join_time1 = new CountDownLatch(2);
		final CountDownLatch join_time2 = new CountDownLatch(2);

		Thread left4_thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					RandomAccessFile raf1 = new RandomAccessFile(new File(fileName), "rw");
					MappedByteBuffer mapBuf1 = raf1.getChannel().map(MapMode.READ_WRITE, 0, 8);
					
					join_start.await();
					
					// time1
					writeBuf(mapBuf1, 0, "aaaa");
					join_time1.countDown();
					
					
					join_time1.await();
					String left4_mb1 = readBuf(mapBuf1, 0);
					String right4_mb1 = readBuf(mapBuf1, 4);
					System.out.println(String.format("left4_mb1: %s, right4_mb1: %s", left4_mb1, right4_mb1));
					
					// time2
					writeBuf(mapBuf1, 0, "cccc");
					join_time2.countDown();
					
					join_time2.await();
					left4_mb1 = readBuf(mapBuf1, 0);
					right4_mb1 = readBuf(mapBuf1, 4);
					System.out.println(String.format("left4_mb1: %s, left4_mb1: %s", left4_mb1, right4_mb1));
					
					raf1.close();
					
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				
				
			}
			
		}, "left4_thread");
		
		
		Thread right4_thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					RandomAccessFile raf2 = new RandomAccessFile(new File(fileName), "rw");
					MappedByteBuffer mapBuf2 = raf2.getChannel().map(MapMode.READ_WRITE, 0, 8);
					
					// time1
					writeBuf(mapBuf2, 4, "bbbb");
					join_time1.countDown();
					
					
					join_time1.await();
					String left4_mb2 = readBuf(mapBuf2, 0);
					String right4_mb2 = readBuf(mapBuf2, 4);
					System.out.println(String.format("left4_mb2: %s, right4_mb2: %s", left4_mb2, right4_mb2));
					
					// time2
					writeBuf(mapBuf2, 4, "dddd");
					join_time2.countDown();
					
					
					join_time2.await();
					left4_mb2 = readBuf(mapBuf2, 0);
					right4_mb2 = readBuf(mapBuf2, 4);
					System.out.println(String.format("left4_mb2: %s, right4_mb2: %s", left4_mb2, right4_mb2));
					
					
					raf2.close();
					
				} catch (Exception e) {
					e.printStackTrace();
				} 
				
			}
			
		}, "right4_thread");
		
		
		left4_thread.start();
		right4_thread.start();
		
		join_start.countDown();
		
		
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
