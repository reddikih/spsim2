package sim.storage.manager.buffer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import sim.Block;
import sim.Parameter;
import sim.statistics.RAPoSDAStats;
import sim.storage.DiskResponse;
import sim.storage.manager.RAPoSDAStorageManager;
import sim.storage.manager.StorageManager;
import sim.storage.manager.cmm.Chunk;
import sim.storage.manager.cmm.RAPoSDACacheMemoryManager;
import sim.storage.manager.cmm.RAPoSDACacheWriteResponse;
import sim.storage.manager.ddm.RAPoSDADataDiskManager;
import sim.storage.util.DiskState;
import sim.storage.util.RequestType;

public class EnergyEfficientBufferManager implements IBufferManager {
	
	private RAPoSDAStorageManager sm;
	private BufferMonitor bufferMonitor;
	
	private RAPoSDADataDiskManager ddm;
	
	public EnergyEfficientBufferManager(StorageManager sm) {
		if (sm == null || sm.getDataDiskManager() == null) {
			throw new IllegalArgumentException(
					"StorageManager or DataDiskManager is null!");
		}
		this.sm = (RAPoSDAStorageManager)sm;
		this.bufferMonitor = new BufferMonitor();
		this.ddm = (RAPoSDADataDiskManager)sm.getDataDiskManager();
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
				RAPoSDACacheWriteResponse cmResponse =
						sm.getCacheMemoryManager().write(b);
				
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
		
		return result;
	}
	
	private DiskResponse writeToDataDisk(double arrivalTime) {
		
		// get all buffered data chunks of active disks.
		List<Integer> spinningDiskIds = getSpinningDiskIds(arrivalTime);
		RAPoSDACacheMemoryManager cmm = sm.getCacheMemoryManager();
		List<Block> baseBlocks = new ArrayList<Block>();
		
		// lambda is the mean arrival ratio of write accesses (blocks/s)
		double lambda = 
				this.bufferMonitor.getMeanArrivalRateOfWriteAccesses(arrivalTime);
		
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
		
		// set the initial value of results
		int baseBufferSize = baseBlocks.size();
		
		double baseTimeToNextBufferOverflow = baseBufferSize / lambda;
		// minimum energy efficiency of the base.
		double minEE = 1 / baseTimeToNextBufferOverflow;
		
		// buffer data to be written to the data disks.
		List<Block> toWriteBuffer = new ArrayList<Block>();
		toWriteBuffer.addAll(baseBlocks);

		// loop for all standby disks
		List<Integer> standbyDiskIds = getStandbyDiskIds(arrivalTime);
		List<BufferOfADisk> bufferList = new ArrayList<BufferOfADisk>();
		
		for (int diskId : standbyDiskIds) {
			List<Chunk> chunks = cmm.getBufferChunks(
					diskId,
					ddm.getNumberOfDataDisks(),
					sm.getNumberOfReplica());
			BufferOfADisk buffer = new BufferOfADisk(diskId);
			for (Chunk chunk : chunks) {
				buffer.addBlocks(chunk.getBlocks());
			}
			bufferList.add(buffer);
		}
		
		// energy efficiency of this standby disks and base.
		double tempEE = Double.MAX_VALUE;
		
		// sort the buffered data order by descending of its size.
		BufferOfADisk[] sortedBuffers = sortBufferedData(bufferList);
		int subsetOfStandbyDisks = 1;
		for (int i = sortedBuffers.length - 1; i >= 0; i--) {
			double ttnbo = 0.0; // the time to the next buffer overflow
			ttnbo = (toWriteBuffer.size() + sortedBuffers[i].getSize()) / lambda;
			tempEE = (subsetOfStandbyDisks * Parameter.HDD_SPINUP_ENERGY) / ttnbo;
			
			if (tempEE < minEE) {
				toWriteBuffer.addAll(sortedBuffers[i].getBlocks());
				minEE = tempEE;
				subsetOfStandbyDisks++;
			}
		}
		
		// return the most energy efficient chunks
		Block[] toWrite = toWriteBuffer.toArray(new Block[0]);
		sm.updateArrivalTimeOfBlocks(toWrite, arrivalTime);
		DiskResponse result = ddm.write(toWrite);
		return result;
	}
	
	/*
	 * sort by merge sort algorithm 
	 * @param buffer
	 */
	private BufferOfADisk[] sortBufferedData(
			List<BufferOfADisk> buffers) {
		BufferOfADisk[] mergedBufferedData = 
				buffers.toArray(new BufferOfADisk[0]); 
		mergeSort(mergedBufferedData);
		return mergedBufferedData;
	}
	
	private void mergeSort(BufferOfADisk[] buffers) {
		if (buffers.length <= 1) return;
		
        int m = buffers.length / 2;  
        int n = buffers.length - m;  
        BufferOfADisk[] buf1 = new BufferOfADisk[m];  
        BufferOfADisk[] buf2 = new BufferOfADisk[n];  
        for (int i = 0; i < m; i++) {  
            buf1[i] = buffers[i];  
        }  
        for (int i = 0; i < n; i++) {  
            buf2[i] = buffers[m + i];  
        }  
        mergeSort(buf1);  
        mergeSort(buf2);  
        merge(buf1, buf2, buffers);
	}
	
	private void merge(
			BufferOfADisk[] buf1,
			BufferOfADisk[] buf2,
			BufferOfADisk[] mergedBuffer) {
		
		int i = 0, j = 0;
		while (i < buf1.length || j < buf2.length) {
			if (j >= buf2.length || 
				(i < buf1.length && buf1[i].getSize() < buf2[j].getSize())) {
				mergedBuffer[i + j] = buf1[i];
				i++;
			} else {
				mergedBuffer[i + j] = buf2[j];
				j++;
			}
		}
	}
	
	
	private List<Integer> getSpinningDiskIds(double timestamp) {
		List<DiskState> states = new ArrayList<DiskState>();
		states.add(DiskState.ACTIVE);
		states.add(DiskState.IDLE);
		return ddm.getSpecificStateDiskIds(timestamp, states);
	}
	
	private List<Integer> getStandbyDiskIds(double timestamp) {
		List<DiskState> states = new ArrayList<DiskState>();
		states.add(DiskState.STANDBY);
		return ddm.getSpecificStateDiskIds(timestamp, states);
	}
	
	private class BufferOfADisk {

		private int diskId;
		private List<Block> blocks = new ArrayList<Block>();
		
		public BufferOfADisk(int diskId) {
			this.diskId = diskId;
		}
		
		public void addBlocks(Collection<Block> blocks) {
			this.blocks.addAll(blocks);
		}
		
		public int getDiskId() {
			return this.diskId;
		}
		
		public int getSize() {
			return this.blocks.size();
		}
		
		public List<Block> getBlocks() {
			return this.blocks;
		}
	}
}
