package sim.storage.util;

public class DiskInfo {

	private int diskId;
	private DiskState diskState;
	private ReplicaLevel repLevel;

	public DiskInfo(int diskId, DiskState diskState, ReplicaLevel repLevel) {
		this.diskId = diskId;
		this.diskState = diskState;
		this.repLevel = repLevel;
	}

	public int getDiskId() {
		return diskId;
	}

	public DiskState getDiskState() {
		return diskState;
	}

	public ReplicaLevel getRepLevel() {
		return repLevel;
	}
}
