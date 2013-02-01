package sim.storage.state;

import sim.storage.util.DiskState;

public class DataDiskStateManager extends StateManager {

	private double spindownThreshold;

	public DataDiskStateManager(
			double spindownThreshold, DiskStateParameter parameter) {
		super(parameter);

		this.spindownThreshold = spindownThreshold;
	}

	public DiskState getState(double arrivalTime, double lastActiveTime) {
		// if disk access is the first, then the disk state is IDLE.
		if (lastActiveTime == 0)
			return DiskState.IDLE;

		DiskState result;
		double delta = arrivalTime - lastActiveTime;

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
		case SPINDOWN :
			start = lastArrivalTime;
			end = lastActiveTime;
			energy = calcEnergy(DiskState.ACTIVE, end - start);
			if (energy > 0) {
				// TODO log energy
			}

			start = end;
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

			latency = (end + parameter.getSpinupTime()) - updateTime;
			break;
		case STANDBY :
			start = lastArrivalTime;
			end = lastActiveTime;
			energy = calcEnergy(DiskState.ACTIVE, end - start);
			if (energy > 0) {
				// TODO log energy
			}

			start = end;
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

			// calculate spindown energy
			b = e;
			e = accessTime;
			energy = calcEnergy(DiskState.SPINDOWN, e - b);

			break;
		case STANDBY:
			// calculate idle energy
			b = lastIdleStartTime;
			e = b + spindownThreshold;
			energy = calcEnergy(DiskState.IDLE, e - b);

			// calculate spindown energy
			b = e;
			e = b + parameter.getSpindownTime();
			energy = calcEnergy(DiskState.SPINDOWN, e - b);

			// calculate standby energy
			b = e;
			e = accessTime;
			energy = calcEnergy(DiskState.SPINDOWN, e - b);

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

		return tempDelay + parameter.getSpinupTime();
	}
}
