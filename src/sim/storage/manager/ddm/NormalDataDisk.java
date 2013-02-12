package sim.storage.manager.ddm;

import sim.Block;
import sim.statistics.RAPoSDAStats;
import sim.storage.HDDParameter;
import sim.storage.HardDiskDrive;
import sim.storage.state.WithoutSleepDiskStateManager;
import sim.storage.util.DiskState;
import sim.storage.util.RequestType;

public class NormalDataDisk extends HardDiskDrive {

	private WithoutSleepDiskStateManager stm;

	public NormalDataDisk(
			int id,
			HDDParameter parameter,
			WithoutSleepDiskStateManager stm) {

		super(id, parameter);
		this.stm = stm;
	}

	@Override
	public double read(Block[] blocks) {
		double arrivalTime = blocks[0].getAccessTime();
		double latency = stm.stateUpdate(this, arrivalTime, lastIdleStartTime);
		double responseTime = super.read(blocks);

		stm.postStateUpdate(
				this,
				DiskState.ACTIVE,
				arrivalTime + latency,
				arrivalTime + responseTime);

		// TODO Prepare normal stats class.
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

		// TODO Prepare normal stats class.
		// log access count
		RAPoSDAStats.addDataDiskResponseTime(responseTime);

		// record write count statistics
		RAPoSDAStats.incrementDataDiskAccessCount(RequestType.WRITE);

		return responseTime;
	}

	public DiskState getState(double accessTime) {
		return stm.getState(accessTime, lastIdleStartTime);
	}

	public void close(double closeTime) {
		stm.stateUpdate(this, closeTime, lastIdleStartTime);
	}

}
