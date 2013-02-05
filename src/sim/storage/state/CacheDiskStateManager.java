package sim.storage.state;

import sim.statistics.RAPoSDAStats;
import sim.storage.HardDiskDrive;
import sim.storage.util.DiskState;

public class CacheDiskStateManager extends StateManager {

	private static String format = "CacheDisk[%d] State:%s Energy:%.2f time:%.3f start:%.4f end:%.4f\n";

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
			HardDiskDrive cd,
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
				RAPoSDAStats.addEnergy(energy, DiskState.IDLE);
				// TODO to be replaced with logging library
				System.out.printf(
						CacheDiskStateManager.format,
						cd.getId(),
						DiskState.IDLE,
						energy,
						end - start,
						start,
						end);
			}
			break;
		default:
			throw new IllegalDiskStateException(
					"In this case, the disk state should be ACTIVE or IDLE");
		}
		return latency;
	}

	@Override
	public void postStateUpdate(
			HardDiskDrive disk, DiskState state, double start, double end) {
		if (end < start)
			throw new IllegalArgumentException(
					"start time should be less than equal end time");

		double energy = calcEnergy(state, end - start);
		RAPoSDAStats.addEnergy(energy, state);
		// TODO to be replaced with logging library
		System.out.printf(
				CacheDiskStateManager.format,
				disk.getId(),
				state,
				energy,
				end - start,
				start,
				end);
	}

}
