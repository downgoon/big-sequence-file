package com.github.downgoon.bsf.example;

import java.io.IOException;

import com.github.downgoon.bsf.BigSequenceFile;

public class QuickStart {

	public static void main(String[] args) throws IOException {
		BigSequenceFile bsf = null;

		try {
			bsf = new BigSequenceFile("/Users/liwei/tmp/bsf/hello.bsf");
			bsf.open();
			
			System.out.println("bsf init: " + bsf);
			
			new Thread(new WatchingReader(bsf), "watching-reader").start();

			bsf.appendTrunk("abc".getBytes());
			System.out.println("bsf after wrting 'abc': " + bsf);

			bsf.appendTrunk("def".getBytes());
			System.out.println("bsf after wrting 'def': " + bsf);

			bsf.appendTrunk("g".getBytes());
			System.out.println("bsf after wrting 'g': " + bsf);

			

		} finally {
			if (bsf != null) {
				bsf.close();
			}
		}
	}

	static class WatchingReader implements Runnable {
		private BigSequenceFile bsf;

		public WatchingReader(BigSequenceFile bsf) {
			this.bsf = bsf;
		}

		@Override
		public void run() {
			while (true) {
				byte[] trunk = bsf.deductTrunk();
				System.out.println("\t<<< bsf reading: " + new String(trunk) + " | with meta: " + bsf);

				if (trunk == null) {
					System.out.println("\t<<< bsf read NULL");
				} else {
					System.out.println(new String(trunk));
				}
			}

		}

	}

}
