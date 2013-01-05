package sim.storage.manager.cmm;

import sim.Block;

public class RAPoSDACacheWriteResponse {

	private double responseTime;
	private int maxBufferDiskId;
	private Block[] overflows;

	public RAPoSDACacheWriteResponse(double responseTime, int maxBufferDiskId, Block[] overflows) {
		this.responseTime = responseTime;
		this.maxBufferDiskId = maxBufferDiskId;
		this.overflows = overflows;
	}

	public double getResponseTime() {
		return responseTime;
	}

	public int getMaxBufferDiskId() {
		return maxBufferDiskId;
	}

	public Block[] getOverflows() {
		return overflows;
	}
}
