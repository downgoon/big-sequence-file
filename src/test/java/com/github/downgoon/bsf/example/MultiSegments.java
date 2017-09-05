package com.github.downgoon.bsf.example;

import com.github.downgoon.bsf.BigSequenceFile;
import com.github.downgoon.bsf.conf.BSFConf;

public class MultiSegments {

	public static void main(String[] args) throws Exception {
		BSFConf bsfConf = BSFConf.conf();
		
		bsfConf.setSegmentLimitBytes(30);
		bsfConf.setTrunkLimitCount(50); // 5 or 50
		
		BigSequenceFile bsf = null;

		try {
			bsf = new BigSequenceFile("/Users/liwei/tmp/bsf/multi.bsf");
			bsf.open();
			
			System.out.println("bsf init: " + bsf);
			
			Thread wachingReader = new Thread(new WatchingReader(bsf), "watching-reader");
			
			for (int i = 0; i < 6; i ++) {
				String trunk = "abc" + i;
				bsf.appendTrunk(trunk.getBytes());
				System.out.println(">>> append " + i + " : " + bsf);
				Thread.sleep(100L);
			}
			
			wachingReader.start();
			
			

		} finally {
			if (bsf != null) {
				// bsf.close(); // Don't close
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
			int count = 0;
			while (true) {
				byte[] trunk = bsf.deductTrunk();
				System.out.println("\t<<< deduct  " + count + " " + new String(trunk) + ": " + bsf);
				count ++;
			}

		}

	}

}
