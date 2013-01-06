package sim.storage.util;

public class DiskState {

	private int diskId;
	private State state;
	private ReplicaLevel repLevel;

	public DiskState(int diskId, State state, ReplicaLevel repLevel) {
		this.diskId = diskId;
		this.state = state;
		this.repLevel = repLevel;
	}

	public int getDiskId() {
		return diskId;
	}

	public State getState() {
		return state;
	}

	public ReplicaLevel getRepLevel() {
		return repLevel;
	}

	public static enum State {
		ACTIVE,
		IDLE,
		STANDBY,
		SPINDOWN,
		SPINUP,
	}
}
