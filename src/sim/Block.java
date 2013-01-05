package sim;

import java.math.BigInteger;

import sim.storage.util.ReplicaLevel;

public class Block {

	public final static int BLOCK_SIZE = 4096;
	public final static Block NULL = new Block(new BigInteger("-1"), null, Double.MIN_VALUE, -1);

	private BigInteger id;
	private ReplicaLevel repLevel;
	private double accessTime;
	private int diskId;

	public Block(BigInteger id, ReplicaLevel repLevel, double accessTime, int diskId) {
		this.id = id;
		this.repLevel = repLevel;
		this.accessTime = accessTime;
		this.diskId = diskId;
	}

	public double getAccessTime() {
		return accessTime;
	}

	public void setAccessTime(double accessTime) {
		this.accessTime = accessTime;
	}

	public BigInteger getId() {
		return id;
	}

	public ReplicaLevel getRepLevel() {
		return repLevel;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof Block)) return false;
		Block target = (Block)obj;
		return  target.getId().equals(this.getId());
	}

	@Override
	public int hashCode() {
		return this.getId().hashCode();
	}

	public int getDiskId() {
		return diskId;
	}

}
