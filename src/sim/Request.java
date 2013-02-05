package sim;

import sim.storage.util.RequestType;

public class Request {

	private long key;
	private int size;
	private double arrvalTime;
	private RequestType type;

	public Request(long key, int size, double arrivalTime, RequestType type) {
		this.key = key;
		this.size = size;
		this.arrvalTime = arrivalTime;
		this.type = type;
	}

	public long getKey() {
		return key;
	}

	public int getSize() {
		return size;
	}

	public double getArrvalTime() {
		return arrvalTime;
	}

	public RequestType getType() {
		return type;
	}
}
