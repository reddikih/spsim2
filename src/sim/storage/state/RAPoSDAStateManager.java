package sim.storage.state;

import sim.storage.util.DiskState;

public class RAPoSDAStateManager {

	private double spindownThreshold;
	private DiskStateParameter parameter;

	public RAPoSDAStateManager(
			double spindownThreshold, DiskStateParameter parameter) {

		this.spindownThreshold = spindownThreshold;
		this.parameter = parameter;
	}

	public DiskState getState(double arrivalTime, double lastActiveTime) {
		DiskState result;
		double delta = arrivalTime - lastActiveTime;

		if (delta < 0) {
			// Active中に再アクセス
			result = DiskState.ACTIVE;
		} else if (delta <= spindownThreshold) {
			// Idle中に再アクセス
			result = DiskState.IDLE;
		} else if (delta <= (spindownThreshold + parameter.getSpindownTime())) {
			// Spindown中に再アクセス
			result = DiskState.SPINDOWN;
		} else {
			// Standby中に再アクセス
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

	private double calcEnergy(DiskState state, double time) {
		double result = 0.0;

		switch (state) {
		case ACTIVE :
			result = parameter.getActivePower() * time;
			break;
		case IDLE :
			result = parameter.getIdlePower() * time;
			break;
		case SPINDOWN :
			result = parameter.getSpindownEnergy();
			break;
		case STANDBY :
			result = parameter.getStandbyPower() * time;
			break;
		case SPINUP :
			result = parameter.getSpinupEnergy();
			break;
		}
		return result;
	}

}
