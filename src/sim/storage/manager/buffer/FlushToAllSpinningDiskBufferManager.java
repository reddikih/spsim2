package sim.storage.manager.buffer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sim.Block;
import sim.storage.DiskResponse;
import sim.storage.manager.StorageManager;
import sim.storage.manager.cmm.Chunk;
import sim.storage.manager.cmm.RAPoSDACacheMemoryManager;
import sim.storage.manager.ddm.RAPoSDADataDiskManager;

public class FlushToAllSpinningDiskBufferManager extends BufferManager {

	public FlushToAllSpinningDiskBufferManager(StorageManager sm) {
		super(sm);
	}

	@Override
	protected DiskResponse writeToDataDisk(double arrivalTime) {
		int maxBufferDiskId = this.cmResponse.getMaxBufferDiskId();
		RAPoSDADataDiskManager ddm = 
				(RAPoSDADataDiskManager)sm.getDataDiskManager();
		
		// log header
		logHeader(arrivalTime);
		
		if(!ddm.isSpinning(maxBufferDiskId, arrivalTime))
			ddm.spinUp(maxBufferDiskId, arrivalTime);
		
		List<Block> spinningDisksBuffer = getSpinningDisksBuffer(arrivalTime);
		
		Block[] blocks = extraUniqueBlocks(
				spinningDisksBuffer, this.cmResponse.getOverflows());
		
		sm.updateArrivalTimeOfBlocks(blocks, arrivalTime);
		
		DiskResponse ddResponse = ddm.write(blocks); 
		
		// log footer
		logFooter(Arrays.asList(maxBufferDiskId), ddResponse.getResults());
		
		return ddResponse;
	}
	
	private List<Block> getSpinningDisksBuffer(double arrivalTime) {
		// get all buffered data chunks of active disks.
		List<Integer> spinningDiskIds = getSpinningDiskIds(arrivalTime);
		List<Block> baseBlocks = new ArrayList<Block>();

		RAPoSDACacheMemoryManager cmm = sm.getCacheMemoryManager();
		RAPoSDADataDiskManager ddm = (RAPoSDADataDiskManager)sm.getDataDiskManager();
		
		for (int diskId : spinningDiskIds) {
			List<Chunk> chunks = cmm.getBufferChunks(
					diskId,
					ddm.getNumberOfDataDisks(),
					sm.getNumberOfReplica());
			for (Chunk chunk : chunks) {
				baseBlocks.addAll(chunk.getBlocks());
				chunk = null;
			}
		}
		return baseBlocks;
	}
	
}
