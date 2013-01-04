package sim.storage;

import sim.Block;

public class CacheResponse {

	private double responseTime;
	private Block[] returnBlocks;

	public CacheResponse(double responseTime, Block[] returnBlocks) {
		this.responseTime = responseTime;
		this.returnBlocks = returnBlocks;
	}

	public double getResponseTime() {
		return responseTime;
	}

	public Block[] getReturnBlocks() {
		return returnBlocks;
	}

}
