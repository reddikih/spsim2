package sim.storage;

public class CacheParameter {

	/** Capacity threshold ratio of cache memory. (0.0 - 1.0) */
	private final double threshold;
	/** Cache memory size. 4GB by default. */
	private final long capacity;
	/** Memory access latency represents by second. */
	private final double latency;

	public CacheParameter(double threshold, long capacity, double latency) {
		this.threshold = threshold;
		this.capacity = capacity;
		this.latency = latency;
	}

	public double getThreshold() {
		return threshold;
	}

	public long getCapacity() {
		return capacity;
	}

	public double getLatency() {
		return latency;
	}

}
