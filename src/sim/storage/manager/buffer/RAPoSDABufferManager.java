package sim.storage.manager.buffer;

import java.util.Arrays;

import sim.Block;
import sim.storage.DiskResponse;
import sim.storage.manager.StorageManager;
import sim.storage.manager.ddm.RAPoSDADataDiskManager;

public class RAPoSDABufferManager extends BufferManager {
	
	
	public RAPoSDABufferManager(StorageManager sm) {
		super(sm);
	}

	protected DiskResponse writeToDataDisk(double arrivalTime) {
		
		int maxBufferDiskId = this.cmResponse.getMaxBufferDiskId();
		RAPoSDADataDiskManager ddm = 
				(RAPoSDADataDiskManager)sm.getDataDiskManager();
		
		// log header
		logHeader(arrivalTime);
		
		if(!ddm.isSpinning(maxBufferDiskId, arrivalTime)) {
			ddm.spinUp(maxBufferDiskId, arrivalTime);
		}
		
		Block[] blocks = this.cmResponse.getOverflows();
		sm.updateArrivalTimeOfBlocks(blocks, arrivalTime);
		
		DiskResponse ddResponse = ddm.write(blocks);
		
		// log footer
		logFooter(Arrays.asList(maxBufferDiskId), ddResponse.getResults());

		return ddResponse; 
	}

}
