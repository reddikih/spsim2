package sim.storage;

import sim.Block;

public class DiskResponse {

	private double responseTime;
	private Block[] results;

	public DiskResponse(double responseTime, Block[] results) {
		this.responseTime = responseTime;
		this.results = results;
	}

	public double getResponseTime() {
		return responseTime;
	}

	public Block[] getResults() {
		return results;
	}

}
