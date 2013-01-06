package sim.storage.manager.ddm;

import sim.Block;
import sim.storage.HDDParameter;
import sim.storage.HardDiskDrive;
import sim.storage.state.RAPoSDAStateManager;
import sim.storage.util.DiskState;

public class DataDisk extends HardDiskDrive {

	private RAPoSDAStateManager stm;

	public DataDisk(
			int id,
			HDDParameter parameter,
			RAPoSDAStateManager stm) {

		super(id, parameter);
		this.stm = stm;
	}

	@Override
	public double read(Block[] blocks) {
		double arrivalTime = blocks[0].getAccessTime();
		double delay =
			stm.stateUpdate(arrivalTime, lastArrivalTime, lastResponseTime);
		updateArrivalTimeOfBlocks(blocks, arrivalTime + delay);
		return super.read(blocks);
	}

	@Override
	public double write(Block[] blocks) {
		double arrivalTime = blocks[0].getAccessTime();
		double delay =
			stm.stateUpdate(arrivalTime, lastArrivalTime, lastResponseTime);
		updateArrivalTimeOfBlocks(blocks, arrivalTime + delay);
		return super.write(blocks);
	}

	public DiskState getState(double accessTime) {
		return stm.getState(accessTime, lastArrivalTime + lastResponseTime);
	}

	public double stateUpdate(double updateTime) {
		return stm.stateUpdate(updateTime, lastArrivalTime, lastResponseTime);
	}

	// Duplicate with RAPoSDAStorageManager.updateArrivalTimeOfBlocks()
	private void updateArrivalTimeOfBlocks(Block[] blocks, double arrivalTime) {
		for (Block block : blocks) {
			block.setAccessTime(arrivalTime);
		}
	}
}
