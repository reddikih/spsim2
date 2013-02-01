package sim.storage.manager.ddm;

import sim.Block;
import sim.storage.HDDParameter;
import sim.storage.HardDiskDrive;
import sim.storage.state.DataDiskStateManager;
import sim.storage.util.DiskState;

public class DataDisk extends HardDiskDrive {

	private DataDiskStateManager stm;

	public DataDisk(
			int id,
			HDDParameter parameter,
			DataDiskStateManager stm) {

		super(id, parameter);
		this.stm = stm;
	}

	@Override
	public double read(Block[] blocks) {
		double arrivalTime = blocks[0].getAccessTime();
		stm.stateUpdate(arrivalTime, lastArrivalTime, lastResponseTime);
		return super.read(blocks);
	}

	@Override
	public double write(Block[] blocks) {
		double arrivalTime = blocks[0].getAccessTime();
		stm.stateUpdate(arrivalTime, lastArrivalTime, lastResponseTime);
		return super.write(blocks);
	}

	public DiskState getState(double accessTime) {
		return stm.getState(accessTime, lastArrivalTime + lastResponseTime);
	}

	public double stateUpdate(double updateTime) {
		double latency = stm.stateUpdate(
				updateTime, lastArrivalTime, lastResponseTime);
		lastArrivalTime = lastArrivalTime + lastResponseTime + latency;
		lastResponseTime = 0.0;
		return latency;
	}

	public boolean isSpinning(double accessTime) {
		DiskState state = getState(accessTime);
		return DiskState.ACTIVE == state || DiskState.IDLE == state;
	}

	public double spinUp(double accessTime) {
		return stm.spinUp(accessTime, lastArrivalTime + lastResponseTime);
	}

	public double getStandbyTime(double accessTime) {
		double result = accessTime - (lastArrivalTime + lastResponseTime);
		return result < 0 ? 0 : result;
	}
}
