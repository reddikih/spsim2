package sim.storage.util;

public enum ReplicaLevel {
	ZERO(0), // primary
	ONE(1),  // secondary
	TWO(2);  // and so on.

	private int value;

	private ReplicaLevel(int value) {
		this.value = value;
	}

	public int getValue() {
		return this.value;
	}
}
