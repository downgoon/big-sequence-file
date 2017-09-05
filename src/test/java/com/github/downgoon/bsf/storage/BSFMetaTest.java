package com.github.downgoon.bsf.storage;

import java.io.File;
import java.nio.file.Files;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.github.downgoon.bsf.conf.BSFProtocol;
import com.github.downgoon.bsf.storage.BSFMeta;
import junit.framework.Assert;

public class BSFMetaTest  {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testMeta() throws Exception {
		File dir = Files.createTempDirectory("bsftest").toFile();
		System.out.println("tmp dir: " + dir.getAbsolutePath());

		BSFMeta meta = null;
		try {
			meta = new BSFMeta(dir + File.separator + "hello");
			meta.open();

			System.out.println(meta);

			Assert.assertEquals(0, meta.getTrunkCount());
			Assert.assertEquals(0, meta.getWriteSegmentNumber());
			Assert.assertEquals(12, meta.getWriteSegmentOffset());
			Assert.assertEquals(0, meta.getReadSegmentNumber());
			Assert.assertEquals(12, meta.getReadSegmentOffset());

			meta.setWriteSegmentNumber((short) 1);
			meta.setTrunkCount(100);
			meta.incrTrunkCount();

			System.out.println(meta);

			Assert.assertEquals(101, meta.getTrunkCount());
			Assert.assertEquals(1, meta.getWriteSegmentNumber());
			Assert.assertEquals(12, meta.getWriteSegmentOffset());
			Assert.assertEquals(0, meta.getReadSegmentNumber());
			Assert.assertEquals(12, meta.getReadSegmentOffset());

			meta.setReadSegmentNumber((short) 1);
			meta.setReadSegmentOffset(20);
			meta.setWriteSegmentNumber((short) 3);
			meta.setWriteSegmentOffset(40);
			meta.setTrunkCount(205);
			meta.decrTrunkCount();

			System.out.println(meta);

			Assert.assertEquals(204, meta.getTrunkCount());
			Assert.assertEquals(3, meta.getWriteSegmentNumber());
			Assert.assertEquals(40, meta.getWriteSegmentOffset());
			Assert.assertEquals(1, meta.getReadSegmentNumber());
			Assert.assertEquals(20, meta.getReadSegmentOffset());

			meta.close();

			meta.open();

			System.out.println(meta);
			Assert.assertEquals(204, meta.getTrunkCount());
			Assert.assertEquals(3, meta.getWriteSegmentNumber());
			Assert.assertEquals(40, meta.getWriteSegmentOffset());
			Assert.assertEquals(1, meta.getReadSegmentNumber());
			Assert.assertEquals(20, meta.getReadSegmentOffset());

			// check file contents
			TestTools.checkFileContent("BF3F1410000000CC000300010000002800000014FFFFFFFF", meta.getName(),
					BSFProtocol.META_HEAD_SIZE);

		} finally {
			meta.close();
			if (dir != null) {
				// remove tmp dir
				new TestTools(true).deleteDirRecur(dir);
			}
		}

	}

}
