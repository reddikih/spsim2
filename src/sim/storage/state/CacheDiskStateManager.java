package sim.storage.state;

import sim.storage.util.DiskState;

public class CacheDiskStateManager extends StateManager {

	public CacheDiskStateManager(DiskStateParameter parameter) {
		super(parameter);
	}

	@Override
	public DiskState getState(double arrivalTime, double lastActiveTime) {
		return null;
	}

	@Override
	public double stateUpdate(
			double updateTime,
			double lastArrivalTime,
			double lastResponseTime) {
		return 0.0;
	}

}
