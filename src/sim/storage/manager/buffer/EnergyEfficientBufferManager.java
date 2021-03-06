package sim.storage.manager.buffer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import sim.Block;
import sim.Parameter;
import sim.storage.DiskResponse;
import sim.storage.manager.StorageManager;
import sim.storage.manager.cmm.Chunk;
import sim.storage.manager.cmm.RAPoSDACacheMemoryManager;
import sim.storage.manager.ddm.RAPoSDADataDiskManager;

public class EnergyEfficientBufferManager extends BufferManager {
	
	private RAPoSDADataDiskManager ddm;
	
	public EnergyEfficientBufferManager(StorageManager sm) {
		super(sm);
		if (sm == null || sm.getDataDiskManager() == null) {
			throw new IllegalArgumentException(
					"StorageManager or DataDiskManager is null!");
		}
		this.ddm = (RAPoSDADataDiskManager)sm.getDataDiskManager();
	}

	@Override
	protected DiskResponse writeToDataDisk(double arrivalTime) {
		
		// get all buffered data chunks of active disks.
		List<Integer> spinningDiskIds = getSpinningDiskIds(arrivalTime);
		RAPoSDACacheMemoryManager cmm = sm.getCacheMemoryManager();
		List<Block> baseBlocks = new ArrayList<Block>();
		
		StringBuffer spinningDiskIdString = new StringBuffer();
		StringBuffer standbyDiskIdString = new StringBuffer();
		
		// lambda is the mean arrival ratio of write accesses (blocks/s)
		double lambda = 
				this.bufferMonitor.getMeanArrivalRateOfWriteAccesses(arrivalTime);

		// log
		logger.debug("---------------------------------------------");
		logger.debug(String.format("Over Flow time:%,.4f lambda:%,.4f", arrivalTime, lambda));
		
		
		for (int diskId : spinningDiskIds) {
			List<Chunk> chunks = cmm.getBufferChunks(
					diskId,
					ddm.getNumberOfDataDisks(),
					sm.getNumberOfReplica());
			for (Chunk chunk : chunks) {
				baseBlocks.addAll(chunk.getBlocks());
				chunk = null;
			}
			spinningDiskIdString.append(diskId).append(",");
		}
		
		// set the initial value of results
		int baseBufferSize = baseBlocks.size();
		
		double baseTimeToNextBufferOverflow = baseBufferSize / lambda;
		// minimum energy efficiency of the base.
		double minEE = 1 / baseTimeToNextBufferOverflow;
		
		// log the efficiency of the base
		logger.debug(String.format("Spinning(base) Disk ID:%s", spinningDiskIdString));
		logger.debug(String.format("Base Size:%d baseEE:%,.4f", baseBufferSize, minEE));
		
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
			standbyDiskIdString.append(diskId).append(",");
		}
		
		// debug log
		logger.debug(String.format("Standby Disk ID:%s", standbyDiskIdString));
		
		// energy efficiency of this standby disks and base.
		double tempEE = Double.MAX_VALUE;
		List<Integer> toSpinupDiskIds = new ArrayList<Integer>();
		
		// sort the buffered data order by descending of its size.
		BufferOfADisk[] sortedBuffers = sortBufferedData(bufferList);
		int subsetOfStandbyDisks = 0;
		double spinupPower = Parameter.HDD_SPINUP_ENERGY / Parameter.HDD_SPINUP_TIME;
		double powerWeight = 0.0;
		int numDisks = sortedBuffers.length;
		for (int i = sortedBuffers.length - 1; i >= 0; i--) {
			double ttnbo = 0.0; // the time to the next buffer overflow
			ttnbo = (toWriteBuffer.size() + sortedBuffers[i].getSize()) / lambda;
			powerWeight = powerWeight + (1 / (numDisks - subsetOfStandbyDisks)) * spinupPower;
			tempEE = powerWeight / ttnbo;
			
			if (tempEE < minEE) {
				toWriteBuffer.addAll(sortedBuffers[i].getBlocks());
				minEE = tempEE;
				toSpinupDiskIds.add(sortedBuffers[i].getDiskId());
				subsetOfStandbyDisks++;
			} else {
				break;
			}
		}
		
		StringBuffer toSpinupDiskIdString = new StringBuffer();
		
		// return the most energy efficient chunks
		for (Integer diskId : toSpinupDiskIds) {
			if (!ddm.isSpinning(diskId, arrivalTime)) {
				ddm.spinUp(diskId, arrivalTime);
			}
			toSpinupDiskIdString.append(diskId).append(",");
		}
		
		Block[] toWrite = toWriteBuffer.toArray(new Block[0]);
		sm.updateArrivalTimeOfBlocks(toWrite, arrivalTime);
		DiskResponse result = ddm.write(toWrite);
		
		// log the selected disks to flush
		logger.debug(String.format("ToSpinup Disk ID:%s", toSpinupDiskIdString));
		logger.debug(String.format("To be Flush Buffer Size:%d mostEE:%,.4f", toWrite.length, minEE));

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
