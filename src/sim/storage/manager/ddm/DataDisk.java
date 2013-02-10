package sim.storage.manager.ddm;

import sim.Block;
import sim.statistics.RAPoSDAStats;
import sim.storage.HDDParameter;
import sim.storage.HardDiskDrive;
import sim.storage.state.DataDiskStateManager;
import sim.storage.util.DiskState;
import sim.storage.util.RequestType;

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
		double latency = stm.stateUpdate(this, arrivalTime, lastIdleStartTime);
		double responseTime = super.read(blocks);

//		arrivalTime += latency;

		stm.postStateUpdate(
				this,
				DiskState.ACTIVE,
				arrivalTime + latency,
				arrivalTime + responseTime);

		// log access count
		RAPoSDAStats.addDataDiskResponseTime(responseTime);

		// record read count statistics
		RAPoSDAStats.incrementDataDiskAccessCount(RequestType.READ);

		return responseTime;
	}

	@Override
	public double write(Block[] blocks) {
		double arrivalTime = blocks[0].getAccessTime();
		double latency = stm.stateUpdate(this, arrivalTime, lastIdleStartTime);
		double responseTime = super.write(blocks);

		stm.postStateUpdate(
				this,
				DiskState.ACTIVE,
				arrivalTime + latency,
				arrivalTime + responseTime);

		// log access count
		RAPoSDAStats.addDataDiskResponseTime(responseTime);

		// record write count statistics
		RAPoSDAStats.incrementDataDiskAccessCount(RequestType.WRITE);

		return responseTime;
	}

	public DiskState getState(double accessTime) {
		return stm.getState(accessTime, lastIdleStartTime);
	}

	public boolean isSpinning(double accessTime) {
		DiskState state = getState(accessTime);
		return DiskState.ACTIVE == state || DiskState.IDLE == state;
	}

	public double spinUp(double accessTime) {
		double delay = stm.spinUp(this, accessTime, lastIdleStartTime);
		lastIdleStartTime = accessTime + delay;
		return delay;
	}

	public void close(double closeTime) {
		stm.stateUpdate(this, closeTime, lastIdleStartTime);
	}

	public double getStandbyTime(double accessTime) {
		// It is assumed that this method is called only when
		// the disk is in the standby state. Thus
		// we can only calculate the following value.
		double result = accessTime - lastIdleStartTime;
		return result < 0 ? 0 : result;
	}
}
