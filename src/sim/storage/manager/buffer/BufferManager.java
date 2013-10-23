package sim.storage.manager.buffer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sim.Block;
import sim.statistics.RAPoSDAStats;
import sim.storage.DiskResponse;
import sim.storage.manager.RAPoSDAStorageManager;
import sim.storage.manager.StorageManager;
import sim.storage.manager.cmm.RAPoSDACacheWriteResponse;
import sim.storage.manager.ddm.RAPoSDADataDiskManager;
import sim.storage.util.DiskState;
import sim.storage.util.RequestType;

public abstract class BufferManager {
	
	protected RAPoSDAStorageManager sm;
	protected BufferMonitor bufferMonitor;
	protected RAPoSDACacheWriteResponse cmResponse;
	
	protected static Logger logger = LoggerFactory.getLogger(BufferManager.class);
	protected static Logger traceLogger = LoggerFactory.getLogger("TRACE_ARRIVAL_TIME");
	
	public BufferManager(StorageManager sm) {
		this.sm = (RAPoSDAStorageManager)sm;
		this.bufferMonitor = new BufferMonitor();
	}
	
	public DiskResponse write(Block[] blocks) {
		double respTime = Double.MIN_VALUE;
		
		List<Block> ddWrittenBlocks = new ArrayList<Block>();
		
		for (Block block : blocks) {
			// block access count log
			RAPoSDAStats.incrementBlockAccessCount(RequestType.WRITE);
			
			Block[] replicas = sm.createReplicas(block);
			for (Block b : replicas) {
				this.cmResponse =
						sm.getCacheMemoryManager().write(b);
				
				// count writes of blocks to monitor arrival rate of buffer
				this.bufferMonitor.addWriteBlockCount(1);

				if (this.cmResponse.getOverflows().length == 0) {
					respTime =
						this.cmResponse.getResponseTime() > respTime
						? this.cmResponse.getResponseTime() : respTime;
				} else { // cache overflow

					// overflow statistics
					RAPoSDAStats.incrementOverflowCount();
					
					double arrivalTime =
						b.getAccessTime() + this.cmResponse.getResponseTime();
					
					// trace log
					traceLogger.trace(
							String.format(
									"arrival_time_at_request:%,.4f arrival_time_at_overflow:%,.4f",
									block.getAccessTime(),
									arrivalTime));

					// write to data disk
					DiskResponse ddResp = writeToDataDisk(arrivalTime);
					
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
		
		this.cmResponse = null;
		
		return result;
	}
	
	abstract protected DiskResponse writeToDataDisk(double arrivalTime);
	
	
	protected List<Integer> getSpinningDiskIds(double timestamp) {
		List<DiskState> states = new ArrayList<DiskState>();
		states.add(DiskState.ACTIVE);
		states.add(DiskState.IDLE);
		RAPoSDADataDiskManager ddm = (RAPoSDADataDiskManager)this.sm.getDataDiskManager();
		return ddm.getSpecificStateDiskIds(timestamp, states);
	}
	
	protected List<Integer> getStandbyDiskIds(double timestamp) {
		List<DiskState> states = new ArrayList<DiskState>();
		states.add(DiskState.STANDBY);
		RAPoSDADataDiskManager ddm = (RAPoSDADataDiskManager)this.sm.getDataDiskManager();
		return ddm.getSpecificStateDiskIds(timestamp, states);
	}
	
	protected Block[] extraUniqueBlocks(List<Block> blockList, Block[] blockArray) {
		if (blockArray.length > 0) {
			for (Block b : blockArray) {
				if (!blockList.contains(b)) {
					blockList.add(b);
				}
			}
		}
		return blockList.toArray(new Block[0]);
	}
	
	protected void logHeader(double arrivalTime) {
		// log
		logger.debug("---------------------------------------------");
		logger.debug(String.format("Over Flow time:%,.4f", arrivalTime));
		
		StringBuffer spinningDiskIdString = new StringBuffer();
		for (Integer diskId : getSpinningDiskIds(arrivalTime)) {
			spinningDiskIdString.append(diskId).append(",");
		}
		StringBuffer standbyDiskIdString = new StringBuffer();
		for (Integer diskId : getStandbyDiskIds(arrivalTime)) {
			standbyDiskIdString.append(diskId).append(",");
		}
		logger.debug(String.format("Spinning(base) Disk ID:%s", spinningDiskIdString));
		logger.debug(String.format("Standby Disk ID:%s", standbyDiskIdString));
	}
	
	protected void logFooter(List<Integer> spinupDiskIds, Block[] writtenBlocks) {
		StringBuffer spinupDiskIdString = new StringBuffer();
		for (int diskId : spinupDiskIds) {
			spinupDiskIdString.append(diskId).append(",");
		}
		logger.debug(String.format("ToSpinup Disk ID:%s", spinupDiskIdString));
		logger.debug(String.format("Actual Flushed Buffer Size:%d", writtenBlocks.length));
	}

}
