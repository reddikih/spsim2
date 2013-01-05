package sim;

public class Response {

	private long key;
	private double responseTime;

	public Response(long key, double responseTime) {
		this.key = key;
		this.responseTime = responseTime;
	}

	public long getKey() {
		return key;
	}

	public double getResponseTime() {
		return responseTime;
	}

}
