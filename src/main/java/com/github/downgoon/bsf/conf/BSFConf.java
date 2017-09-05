package com.github.downgoon.bsf.conf;

public class BSFConf {

	/** segment file size limit in bytes (default is 128MB) */
	private int segmentLimitBytes = 1024 * 1024 * 128;

	/** trunk size limit in bytes (default is 1MB) */
	private int trunkLimitBytes = 1024 * 1024 * 1;

	/**
	 * max amount of trunks over-stocked in big-sequence-file and not consumed
	 * by readers. -1 indicating no limit.
	 */
	private int trunkLimitCount = Integer.MAX_VALUE;

	private static final BSFConf _singleton = new BSFConf();

	private BSFConf() {

	}

	/**
	 * @return configuration items which may be loaded from files or command
	 *         line parameters
	 */
	public static BSFConf conf() {
		return _singleton;
	}

	public int getSegmentLimitBytes() {
		return segmentLimitBytes;
	}

	public void setSegmentLimitBytes(int segmentLimitBytes) {
		if (segmentLimitBytes <= BSFProtocol.SEG_HEAD_SIZE + 4) {
			throw new IllegalArgumentException(
					"segment limit bytes MUST be greater than SEG_HEAD_SIZE: " + (BSFProtocol.SEG_HEAD_SIZE + 4));
		}
		this.segmentLimitBytes = segmentLimitBytes;
	}

	public int getTrunkLimitBytes() {
		return trunkLimitBytes;
	}

	public void setTrunkLimitBytes(int trunkLimitBytes) {
		this.trunkLimitBytes = trunkLimitBytes;
	}

	/**
	 * @return max amount of trunks over-stocked in big-sequence-file and not
	 *         consumed by readers. -1 indicating no limit.
	 */
	public int getTrunkLimitCount() {
		return trunkLimitCount;
	}

	public void setTrunkLimitCount(int trunkLimitCount) {
		this.trunkLimitCount = trunkLimitCount;
	}

}
