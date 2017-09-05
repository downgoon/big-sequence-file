package com.github.downgoon.bsf.storage;

import java.io.File;
import java.nio.file.Files;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.github.downgoon.bsf.conf.BSFProtocol;
import com.github.downgoon.bsf.storage.BSFMeta;
import com.github.downgoon.bsf.storage.BSFSegment;
import junit.framework.Assert;

public class BSFSegmentTest  {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testAppend() throws Exception {
		File dir = Files.createTempDirectory("bsftest").toFile();
		System.out.println("tmp dir: " + dir.getAbsolutePath());

		BSFMeta meta = null;
		BSFSegment seg0 = null;
		try {
			meta = new BSFMeta(dir + File.separator + "hello");
			meta.open();
			seg0 = meta.openSegment((short) 0);

			System.out.println("meta init: " + meta);
			System.out.println("seg0 init: " + seg0);
			Assert.assertEquals(0, meta.getTrunkCount());
			Assert.assertEquals(0, meta.getWriteSegmentNumber());
			Assert.assertEquals(12, meta.getWriteSegmentOffset());
			Assert.assertEquals(0, meta.getReadSegmentNumber());
			Assert.assertEquals(12, meta.getReadSegmentOffset());
			Assert.assertEquals(0, seg0.getTrunkCount());
			Assert.assertEquals(12, seg0.getUsedSize());

			seg0.appendTrunkIfNotFull("abcd".getBytes());

			System.out.println("meta after 'abcd': " + meta);
			System.out.println("seg0 after 'abcd': " + seg0);
			Assert.assertEquals(1, meta.getTrunkCount());
			Assert.assertEquals(0, meta.getWriteSegmentNumber());
			Assert.assertEquals(20, meta.getWriteSegmentOffset());
			Assert.assertEquals(0, meta.getReadSegmentNumber());
			Assert.assertEquals(12, meta.getReadSegmentOffset());
			Assert.assertEquals(1, seg0.getTrunkCount());
			Assert.assertEquals(20, seg0.getUsedSize());

			seg0.appendTrunkIfNotFull(new byte[] { (short) 0x32 });
			System.out.println("meta after one byte: " + meta);
			System.out.println("seg0 after one byte: " + seg0);
			Assert.assertEquals(2, meta.getTrunkCount());
			Assert.assertEquals(0, meta.getWriteSegmentNumber());
			Assert.assertEquals(25, meta.getWriteSegmentOffset());
			Assert.assertEquals(0, meta.getReadSegmentNumber());
			Assert.assertEquals(12, meta.getReadSegmentOffset());
			Assert.assertEquals(2, seg0.getTrunkCount());
			Assert.assertEquals(25, seg0.getUsedSize());

			seg0.close();
			meta.close();

			// check file contents
			TestTools.checkFileContent("BF3F14100000000200000000000000190000000CFFFFFFFF", meta.getName(),
					BSFProtocol.META_HEAD_SIZE);

			// recove meta and segment head
			BSFMeta metaRecover = new BSFMeta(dir + File.separator + "hello");
			metaRecover.open();
			BSFSegment seg0Recover = meta.openSegment((short) 0);
			Assert.assertEquals(2, metaRecover.getTrunkCount());
			Assert.assertEquals(0, metaRecover.getWriteSegmentNumber());
			Assert.assertEquals(25, metaRecover.getWriteSegmentOffset());
			Assert.assertEquals(0, metaRecover.getReadSegmentNumber());
			Assert.assertEquals(12, metaRecover.getReadSegmentOffset());
			Assert.assertEquals(2, seg0Recover.getTrunkCount());
			Assert.assertEquals(25, seg0Recover.getUsedSize());
			TestTools.checkFileContent("BF3F14100000000200000000000000190000000CFFFFFFFF", metaRecover.getName(),
					BSFProtocol.META_HEAD_SIZE);

			metaRecover.close();
			seg0Recover.close();

		} finally {
			if (dir != null) {
				// remove tmp dir
				new TestTools(true).deleteDirRecur(dir);
			}
		}
	}

	@Test
	public void testDeduct() throws Exception {
		File dir = Files.createTempDirectory("bsftest").toFile();
		System.out.println("tmp dir: " + dir.getAbsolutePath());

		BSFMeta meta = null;
		BSFSegment seg0 = null;
		try {
			meta = new BSFMeta(dir + File.separator + "hello");
			meta.open();
			seg0 = meta.openSegment((short) 0);
			seg0.appendTrunkIfNotFull("abcd".getBytes());
			seg0.appendTrunkIfNotFull("fg".getBytes());

			byte[] abcdBytes = seg0.deductTrunkIfNotEOF();
			Assert.assertEquals("abcd", new String(abcdBytes));

			System.out.println("meta after deduct 'abcd': " + meta);
			System.out.println("seg0 after deduct 'abcd': " + seg0);
			Assert.assertEquals(1, meta.getTrunkCount());
			Assert.assertEquals(0, meta.getWriteSegmentNumber());
			Assert.assertEquals(26, meta.getWriteSegmentOffset());
			Assert.assertEquals(0, meta.getReadSegmentNumber());
			Assert.assertEquals(20, meta.getReadSegmentOffset());
			Assert.assertEquals(1, seg0.getTrunkCount());
			Assert.assertEquals(26, seg0.getUsedSize());

			byte[] fgBytes = seg0.deductTrunkIfNotEOF();
			Assert.assertEquals("fg", new String(fgBytes));
			System.out.println("meta after deduct 'fg': " + meta);
			System.out.println("seg0 after deduct 'fg': " + seg0);
			Assert.assertEquals(0, meta.getTrunkCount());
			Assert.assertEquals(0, meta.getWriteSegmentNumber());
			Assert.assertEquals(26, meta.getWriteSegmentOffset());
			Assert.assertEquals(0, meta.getReadSegmentNumber());
			Assert.assertEquals(26, meta.getReadSegmentOffset());
			Assert.assertEquals(0, seg0.getTrunkCount());
			Assert.assertEquals(26, seg0.getUsedSize());

			byte[] eof = seg0.deductTrunkIfNotEOF();
			Assert.assertNull(eof);

			System.out.println(seg0.getName());
			System.out.println(meta.getName());

			seg0.close();
			meta.close();

		} finally {
			if (dir != null) {
				// remove tmp dir
				new TestTools(true).deleteDirRecur(dir);
			}
		}
	}

}
