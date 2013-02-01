package sim.storage.state;

import sim.storage.util.DiskState;

public class CacheDiskStateManager extends StateManager {

	public CacheDiskStateManager(DiskStateParameter parameter) {
		super(parameter);
	}

	@Override
	public DiskState getState(double arrivalTime, double lastActiveTime) {
		// if disk access is the first, then the disk state is IDLE.
		if (lastActiveTime == 0)
			return DiskState.IDLE;

		DiskState result;
		double delta = arrivalTime - lastActiveTime;

		if (delta < 0) {
			// Access during Active
			result = DiskState.ACTIVE;
		} else {
			// Access during Idle
			result = DiskState.IDLE;
		}
		return result;
	}

	@Override
	public double stateUpdate(
			double updateTime,
			double lastIdleStartTime) {

		double latency = 0.0;
		double start, end;
		double energy;

		DiskState state = getState(updateTime, lastIdleStartTime);
		switch (state) {
		case ACTIVE :
			// There is a few latency but no state change.
			latency = (lastIdleStartTime) - updateTime;
			break;
		case IDLE :
			start = lastIdleStartTime;
			end = updateTime;
			energy = calcEnergy(DiskState.IDLE, end - start);
			if (energy > 0) {
				// TODO log energy
			}
			break;
		default:
			throw new IllegalDiskStateException(
					"In this case, the disk state should be ACTIVE or IDLE");
		}
		return latency;
	}

}
