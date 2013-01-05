package sim;

public class Request {

	private long key;
	private int size;
	private double arrvalTime;

	public Request(long key, int size, double arrivalTime) {
		this.key = key;
		this.size = size;
		this.arrvalTime = arrivalTime;
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
}
