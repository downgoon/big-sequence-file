package com.github.downgoon.bsf.scheduler;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.github.downgoon.bsf.conf.BSFProtocol;

public class SegmentCleaner {

	private ExecutorService executorService;

	public SegmentCleaner() {
		executorService = Executors.newFixedThreadPool(1, new TagThreadFactory("segclean"));
	}

	public void cleanSegment(final String segmentFileName) {

		executorService.submit(new Callable<String>() {

			@Override
			public String call() throws Exception {
				if (!segmentFileName.endsWith(BSFProtocol.SEG_FILE_EXT)) {
					return null; // do nothing
				}
				File segFile = new File(segmentFileName);
				if (segFile.exists()) {
					boolean hasDel = segFile.delete();
					if (!hasDel) {
						throw new IllegalStateException("segment delete fail: " + segmentFileName);
					}
				}
				return segmentFileName;
			}

		});

	}
	
	public void shutdown() {
		executorService.shutdown();
	}

}
