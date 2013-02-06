package sim.storage.state;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sim.statistics.RAPoSDAStats;
import sim.storage.HardDiskDrive;
import sim.storage.manager.ddm.DataDisk;
import sim.storage.util.DiskState;

public class DataDiskStateManager extends StateManager {

	private double spindownThreshold;
	private static Logger logger = LoggerFactory.getLogger(DataDiskStateManager.class);
	private static String format = "DataDisk[%d] State:%s Energy:%.2f time:%.3f start:%.4f end:%.4f\n";

	public DataDiskStateManager(
			double spindownThreshold, DiskStateParameter parameter) {
		super(parameter);

		this.spindownThreshold = spindownThreshold;
	}

	@Override
	public DiskState getState(double arrivalTime, double lastIdleStartTime) {
		// if disk access is the first, then the disk state is IDLE.
		boolean isFirst = false;
		if (lastIdleStartTime == 0)
//			return DiskState.IDLE;
			isFirst = true;

		DiskState result;
		double delta = arrivalTime - lastIdleStartTime;

		if (delta < 0 && !isFirst) {
			// Access during Active
			result = DiskState.ACTIVE;
		} else if (delta <= spindownThreshold) {
			// Access during Idle
			result = DiskState.IDLE;
		} else if (delta <= (spindownThreshold + parameter.getSpindownTime())) {
			// Access during Spindown
			result = DiskState.SPINDOWN;
		} else {
			// Access during Standby
			result = DiskState.STANDBY;
		}
		return result;
	}

	@Override
	public double stateUpdate(
			HardDiskDrive dd, double updateTime, double lastIdleStartTime) {

		double latency = 0.0;
		double start, end;
		double energy;

		DiskState state = getState(updateTime, lastIdleStartTime);
		switch (state) {
		case ACTIVE :
			// There is a few latency but no state change.
			// And no need calc energy of Active state. Beacause
			// it was calculated at previous I/O process.
			latency = lastIdleStartTime - updateTime;
			break;
		case IDLE :
			start = lastIdleStartTime;
			end = updateTime;
			energy = calcEnergy(DiskState.IDLE, end - start);
			if (energy > 0) {
				RAPoSDAStats.addEnergy(energy, DiskState.IDLE);
				logger.trace(
						DataDiskStateManager.format,
						dd.getId(),
						DiskState.IDLE,
						energy,
						end - start,
						start,
						end);
			}
			break;
		case SPINDOWN :
			start = lastIdleStartTime;
			end = start + spindownThreshold;
			energy = calcEnergy(DiskState.IDLE, end - start);
			if (energy > 0) {
				RAPoSDAStats.addEnergy(energy, DiskState.IDLE);
				logger.trace(
						DataDiskStateManager.format,
						dd.getId(),
						DiskState.IDLE,
						energy,
						end - start,
						start,
						end);
			}

			start = end;
			end = updateTime;
			energy = calcEnergy(DiskState.SPINDOWN, end - start);
			if (energy > 0) {
				RAPoSDAStats.addEnergy(energy, DiskState.SPINDOWN);
				logger.trace(
						DataDiskStateManager.format,
						dd.getId(),
						DiskState.SPINDOWN,
						energy,
						end - start,
						start,
						end);
			}
			break;
		case STANDBY :
			start = lastIdleStartTime;
			end = start + spindownThreshold;
			energy = calcEnergy(DiskState.IDLE, end - start);
			if (energy > 0) {
				RAPoSDAStats.addEnergy(energy, DiskState.IDLE);
				logger.trace(
						DataDiskStateManager.format,
						dd.getId(),
						DiskState.IDLE,
						energy,
						end - start,
						start,
						end);
			}

			start = end;
			end = start + parameter.getSpindownTime();
			energy = calcEnergy(DiskState.SPINDOWN, end - start);
			if (energy > 0) {
				RAPoSDAStats.addEnergy(energy, DiskState.SPINDOWN);
				logger.trace(
						DataDiskStateManager.format,
						dd.getId(),
						DiskState.SPINDOWN,
						energy,
						end - start,
						start,
						end);
			}

			start = end;
			end = updateTime;
			energy = calcEnergy(DiskState.STANDBY, end - start);
			if (energy > 0) {
				RAPoSDAStats.addEnergy(energy, DiskState.STANDBY);
				logger.trace(
						DataDiskStateManager.format,
						dd.getId(),
						DiskState.STANDBY,
						energy,
						end - start,
						start,
						end);
			}
			break;
		}

		// log spindown count
		if (state.equals(DiskState.SPINDOWN) || state.equals(DiskState.STANDBY))
			RAPoSDAStats.incrementSpindownCount();

		return latency;
	}

	public double spinUp(DataDisk dd, double accessTime, double lastIdleStartTime) {
		DiskState current = getState(accessTime, lastIdleStartTime);
		double tempDelay = 0.0;
		double start, end, energy;

		switch(current) {
		case SPINDOWN:
			tempDelay =
				(lastIdleStartTime +
				spindownThreshold +
				parameter.getSpindownTime()) - accessTime;

			// calculate idle energy
			start = lastIdleStartTime;
			end = start + spindownThreshold;
			energy = calcEnergy(DiskState.IDLE, end - start);

			RAPoSDAStats.addEnergy(energy, DiskState.IDLE);
			logger.trace(
					DataDiskStateManager.format,
					dd.getId(),
					DiskState.IDLE,
					energy,
					end - start,
					start,
					end);

			// calculate spindown energy
			start = end;
			end = accessTime;
			energy = calcEnergy(DiskState.SPINDOWN, end - start);

			RAPoSDAStats.addEnergy(energy, DiskState.SPINDOWN);
			logger.trace(
					DataDiskStateManager.format,
					dd.getId(),
					DiskState.SPINDOWN,
					energy,
					end - start,
					start,
					end);

			break;
		case STANDBY:
			// calculate idle energy
			start = lastIdleStartTime;
			end = start + spindownThreshold;
			energy = calcEnergy(DiskState.IDLE, end - start);

			RAPoSDAStats.addEnergy(energy, DiskState.IDLE);
			logger.trace(
					DataDiskStateManager.format,
					dd.getId(),
					DiskState.IDLE,
					energy,
					end - start,
					start,
					end);

			// calculate spindown energy
			start = end;
			end = start + parameter.getSpindownTime();
			energy = calcEnergy(DiskState.SPINDOWN, end - start);

			RAPoSDAStats.addEnergy(energy, DiskState.SPINDOWN);
			logger.trace(
					DataDiskStateManager.format,
					dd.getId(),
					DiskState.SPINDOWN,
					energy,
					end - start,
					start,
					end);

			// calculate standby energy
			start = end;
			end = accessTime;
			energy = calcEnergy(DiskState.STANDBY, end - start);

			RAPoSDAStats.addEnergy(energy, DiskState.STANDBY);

			logger.trace(
					DataDiskStateManager.format,
					dd.getId(),
					DiskState.STANDBY,
					energy,
					end - start,
					start,
					end);

			break;
		default:
			throw new IllegalDiskStateException(
					"invalid disk state in Spinning up the current is " +
					current);
		}

		// calculate spinup energy
		start = end;
		end = start + parameter.getSpinupTime();
		energy = calcEnergy(DiskState.SPINUP, end - start);

		RAPoSDAStats.addEnergy(energy, DiskState.SPINUP);
		logger.trace(
				DataDiskStateManager.format,
				dd.getId(),
				DiskState.SPINUP,
				energy,
				end - start,
				start,
				end);

		// log spinup count
		RAPoSDAStats.incrementSpinupCount();

		return tempDelay + parameter.getSpinupTime();
	}

	@Override
	public void postStateUpdate(
			HardDiskDrive disk, DiskState state, double start, double end) {
		double energy = calcEnergy(state, end - start);
		RAPoSDAStats.addEnergy(energy, state);
		logger.trace(
				DataDiskStateManager.format,
				disk.getId(),
				state,
				energy,
				end - start,
				start,
				end);
	}

}