package sim.storage;

import sim.Block;

public class CacheResponse {

	private double responseTime;
	private Block result;

	public CacheResponse(double responseTime, Block result) {
		this.responseTime = responseTime;
		this.result = result;
	}

	public double getResponseTime() {
		return responseTime;
	}

	public Block getResult() {
		return result;
	}

}
