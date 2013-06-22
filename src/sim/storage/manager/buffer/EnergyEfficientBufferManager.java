package sim.storage.manager.buffer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sim.Block;
import sim.statistics.RAPoSDAStats;
import sim.storage.DiskResponse;
import sim.storage.manager.RAPoSDAStorageManager;
import sim.storage.manager.StorageManager;
import sim.storage.manager.cmm.RAPoSDACacheWriteResponse;
import sim.storage.manager.ddm.RAPoSDADataDiskManager;
import sim.storage.util.RequestType;

public class EnergyEfficientBufferManager implements IBufferManager {
	
	private RAPoSDAStorageManager sm;
	private BufferMonitor bufferMonitor;
	
	public EnergyEfficientBufferManager(StorageManager sm) {
		this.sm = (RAPoSDAStorageManager)sm;
		this.bufferMonitor = new BufferMonitor();
	}

	@Override
	public DiskResponse write(Block[] blocks) {
		double respTime = Double.MIN_VALUE;
		
		List<Block> ddWrittenBlocks = new ArrayList<Block>();
		
		for (Block block : blocks) {
			// block access count log
			RAPoSDAStats.incrementBlockAccessCount(RequestType.WRITE);

			Block[] replicas = sm.createReplicas(block);
			for (Block b : replicas) {
				RAPoSDACacheWriteResponse cmResponse = sm.getCacheMemoryManager().write(b);
				
				// count writes of blocks to monitor arrival rate of buffer
				this.bufferMonitor.addWriteBlockCount(1);

				if (cmResponse.getOverflows().length == 0) {
					respTime =
						cmResponse.getResponseTime() > respTime
						? cmResponse.getResponseTime() : respTime;
				} else { // cache overflow

					// overflow statistics
					RAPoSDAStats.incrementOverflowCount();

					double arrivalTime =
						b.getAccessTime() + cmResponse.getResponseTime();

					// write to data disk
					DiskResponse ddResp =
							writeToDataDisk(cmResponse, arrivalTime);
					
					ddWrittenBlocks.addAll(Arrays.asList(ddResp.getResults()));

					// delete blocks on the cache already written in disks.
					double cmDeletedTime = sm.deleteBlocksOnCache(
							ddResp.getResults(),
							ddResp.getResponseTime() + arrivalTime);

					// return least response time;
					double tempResp =
						ddResp.getResponseTime() + cmDeletedTime;

					respTime = respTime < tempResp ? tempResp : respTime;
				}
			}
		}
		
		DiskResponse result = 
				new DiskResponse(respTime, ddWrittenBlocks.toArray(new Block[0]));
		
		return result;
	}
	
	private DiskResponse writeToDataDisk(
			RAPoSDACacheWriteResponse cmResponse,
			double arrivalTime) {
		
		int maxBufferDiskId = cmResponse.getMaxBufferDiskId();
		RAPoSDADataDiskManager ddm = 
				(RAPoSDADataDiskManager)sm.getDataDiskManager();
		
		if(!ddm.isSpinning(maxBufferDiskId, arrivalTime))
			ddm.spinUp(maxBufferDiskId, arrivalTime);
		
		Block[] blocks = cmResponse.getOverflows();
		sm.updateArrivalTimeOfBlocks(blocks, arrivalTime);
		
		return ddm.write(blocks);
	}

}
