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
			double lastArrivalTime,
			double lastResponseTime) {

		double latency = 0.0;
		double start, end;
		double energy;

		double lastActiveTime = lastArrivalTime + lastResponseTime;

		DiskState state = getState(updateTime, lastActiveTime);
		switch (state) {
		case ACTIVE :
			start = lastArrivalTime;
			end = lastActiveTime;
			energy = calcEnergy(DiskState.ACTIVE, end - start);
			if (energy > 0) {
				// TODO log energy
			}

			// There is a few latency but no state change.
			latency = (lastActiveTime) - updateTime;
			break;
		case IDLE :
			start = lastArrivalTime;
			end = lastActiveTime;
			energy = calcEnergy(DiskState.ACTIVE, end - start);
			if (energy > 0) {
				// TODO log energy
			}

			start = end;
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
