package sim.storage.manager.ddm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import sim.Block;
import sim.storage.DiskResponse;
import sim.storage.util.DiskState;

public class RAPoSDADataDiskManager {

	private final int numberOfDataDisks;

	private HashMap<Integer, DataDisk> dataDiskMap;

	public RAPoSDADataDiskManager(
			int numberOfDataDisks,
			HashMap<Integer, DataDisk> dataDiskMap) {

		this.numberOfDataDisks = numberOfDataDisks;
		this.dataDiskMap = dataDiskMap;
	}

	public DiskResponse write(Block[] blocks) {
		List<Block> writtenBlocks = new ArrayList<Block>();
		double arrivalTime = blocks[0].getAccessTime();
		double respTime = Double.MIN_VALUE;
		for (Block block : blocks) {
			int targetDiskId = block.getOwnerDiskId();
			if (isSpinning(targetDiskId, arrivalTime)) {
				DataDisk dd = dataDiskMap.get(targetDiskId);
				assert dd != null;
				double tempResp = dd.write(new Block[]{block});
				respTime = respTime < tempResp ? tempResp : respTime;
				writtenBlocks.add(block);
			}
		}
		return new DiskResponse(respTime, writtenBlocks.toArray(new Block[0]));
	}

	public DiskResponse read(Block[] blocks) {
		double arrivalTime = blocks[0].getAccessTime();
		double respTime = Double.MIN_VALUE;
		for (Block block : blocks) {
			int targetDiskId = block.getOwnerDiskId();
			double latency = 0.0;
			if (!isSpinning(targetDiskId, arrivalTime)) {
				latency = spinUp(targetDiskId, arrivalTime);
			}
			block.setAccessTime(arrivalTime + latency);

			DataDisk dd = dataDiskMap.get(targetDiskId);
			assert dd != null;
			// TODO this can be optimized for sequencial block access.
			double ddResp = dd.read(new Block[]{block});
			respTime = respTime < ddResp ? ddResp : respTime;
		}
		return new DiskResponse(respTime, blocks);
	}

	public boolean isSpinning(int diskId, double accessTime) {
		// TODO Auto-generated method stub
		return true;
	}

	public double spinUp(int diskId, double accessTime) {
		double latency = Double.MAX_VALUE;
		// TODO need implement.
		return latency;
	}

	public int getNumberOfDataDisks() {
		return numberOfDataDisks;
	}

	public List<DiskState> getRelatedDisksState(Block block) {
		// TODO Auto-generated method stub
		return null;
	}

	public DiskState getLongestStandbyDisk(List<DiskState> diskStates) {
		// TODO Auto-generated method stub
		return null;
	}

}
