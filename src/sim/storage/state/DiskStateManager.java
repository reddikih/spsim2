package sim.storage.state;

import sim.storage.HardDiskDrive;
import sim.storage.util.DiskState;

public abstract class DiskStateManager {

	protected DiskStateParameter parameter;

	public DiskStateManager(DiskStateParameter parameter) {
		this.parameter = parameter;
	}

	protected double calcEnergy(DiskState state, double time) {
		double result = 0.0;

		assert time >= 0;

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

	public abstract void postStateUpdate(HardDiskDrive disk, DiskState state, double start, double end);

	public abstract DiskState getState(double arrivalTime, double lastActiveTime);

	public abstract double stateUpdate(HardDiskDrive disk, double updateTime, double lastIdleStartTime);

}
