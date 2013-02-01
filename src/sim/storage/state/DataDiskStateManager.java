package sim.storage.state;

import sim.storage.util.DiskState;

public class DataDiskStateManager extends StateManager {

	private double spindownThreshold;

	public DataDiskStateManager(
			double spindownThreshold, DiskStateParameter parameter) {
		super(parameter);

		this.spindownThreshold = spindownThreshold;
	}

	public DiskState getState(double arrivalTime, double lastIdleStartTime) {
		// if disk access is the first, then the disk state is IDLE.
		if (lastIdleStartTime == 0)
			return DiskState.IDLE;

		DiskState result;
		double delta = arrivalTime - lastIdleStartTime;

		if (delta < 0) {
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

	public double stateUpdate(double updateTime, double lastIdleStartTime) {

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
				// TODO log energy
			}
			break;
		case SPINDOWN :
			start = lastIdleStartTime;
			end = start + spindownThreshold;
			energy = calcEnergy(DiskState.IDLE, end - start);
			if (energy > 0) {
				// TODO log energy
			}

			start = end;
			end = updateTime;
			energy = calcEnergy(DiskState.SPINDOWN, end - start);
			if (energy > 0) {
				// TODO log energy
			}

			latency = (end + parameter.getSpinupTime()) - updateTime;
			break;
		case STANDBY :
			start = lastIdleStartTime;
			end = start + spindownThreshold;
			energy = calcEnergy(DiskState.IDLE, end - start);
			if (energy > 0) {
				// TODO log energy
			}

			start = end;
			end = start + parameter.getSpindownTime();
			energy = calcEnergy(DiskState.SPINDOWN, end - start);
			if (energy > 0) {
				// TODO log energy
			}

			start = end;
			end = updateTime;
			energy = calcEnergy(DiskState.STANDBY, end - start);
			if (energy > 0) {
				// TODO log energy
			}
			break;
		}
		return latency;
	}

	public double spinUp(double accessTime, double lastIdleStartTime) {
		DiskState current = getState(accessTime, lastIdleStartTime);
		double tempDelay = 0.0;
		double b, e, energy;

		switch(current) {
		case SPINDOWN:
			tempDelay =
				(lastIdleStartTime +
				spindownThreshold +
				parameter.getSpindownTime()) - accessTime;

			// calculate idle energy
			b = lastIdleStartTime;
			e = b + spindownThreshold;
			energy = calcEnergy(DiskState.IDLE, e - b);
			// TODO log energy

			// calculate spindown energy
			b = e;
			e = accessTime;
			energy = calcEnergy(DiskState.SPINDOWN, e - b);
			// TODO log energy

			break;
		case STANDBY:
			// calculate idle energy
			b = lastIdleStartTime;
			e = b + spindownThreshold;
			energy = calcEnergy(DiskState.IDLE, e - b);
			// TODO log energy

			// calculate spindown energy
			b = e;
			e = b + parameter.getSpindownTime();
			energy = calcEnergy(DiskState.SPINDOWN, e - b);
			// TODO log energy

			// calculate standby energy
			b = e;
			e = accessTime;
			energy = calcEnergy(DiskState.SPINDOWN, e - b);
			// TODO log energy

			break;
		default:
			throw new IllegalDiskStateException(
					"invalid disk state in Spinning up the current is " +
					current);
		}

		// calculate spinup energy
		b = e;
		e = b + parameter.getSpinupTime();
		energy = calcEnergy(DiskState.SPINDOWN, e - b);
		// TODO log energy

		return tempDelay + parameter.getSpinupTime();
	}
}
