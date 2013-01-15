package sim.storage.state;

import sim.storage.util.DiskState;

public abstract class StateManager {

	protected DiskStateParameter parameter;

	public StateManager(DiskStateParameter parameter) {
		this.parameter = parameter;
	}

	protected double calcEnergy(DiskState state, double time) {
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

	public abstract DiskState getState(double arrivalTime, double lastActiveTime);

	public abstract double stateUpdate(double updateTime, double lastArrivalTime, double lastResponseTime);

}
