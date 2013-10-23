package sim.storage.manager.buffer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import sim.Block;
import sim.Parameter;
import sim.storage.DiskResponse;
import sim.storage.manager.StorageManager;
import sim.storage.manager.cmm.Chunk;
import sim.storage.manager.cmm.RAPoSDACacheMemoryManager;
import sim.storage.manager.ddm.RAPoSDADataDiskManager;

public class SpinupEnergyEfficientDisksBufferManager extends BufferManager {

	public SpinupEnergyEfficientDisksBufferManager(StorageManager sm) {
		super(sm);
	}

	@Override
	protected DiskResponse writeToDataDisk(double arrivalTime) {
		
		int maxBufferDiskId = this.cmResponse.getMaxBufferDiskId();
		RAPoSDADataDiskManager ddm = 
				(RAPoSDADataDiskManager)sm.getDataDiskManager();
		
		// log header
		logHeader(arrivalTime);
		
		if(!ddm.isSpinning(maxBufferDiskId, arrivalTime)) {
			ddm.spinUp(maxBufferDiskId, arrivalTime);
		}
		
		// get to be flushed blocks
		Block[] blocks = getToBeFlushedBlocks(arrivalTime, maxBufferDiskId);
		
		sm.updateArrivalTimeOfBlocks(blocks, arrivalTime);
		
		DiskResponse ddResponse = ddm.write(blocks);
		
		// log footer
		logFooter(Arrays.asList(maxBufferDiskId), ddResponse.getResults());

		return ddResponse;
	}
	
	private List<BufferOfADisk> getStandbyDisksBufferChunks(double arrivalTime) {
		RAPoSDACacheMemoryManager cmm = sm.getCacheMemoryManager();
		RAPoSDADataDiskManager ddm = 
				(RAPoSDADataDiskManager)sm.getDataDiskManager();
		
		List<Integer> standbyDiskIds = getStandbyDiskIds(arrivalTime);
		List<BufferOfADisk> bufferList = new ArrayList<BufferOfADisk>();
		
		StringBuffer standbyDiskIdString = new StringBuffer();
		
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
		logger.debug(String.format(
				"Standby Disk ID(after spin up the max buffered disk in the region):%s", standbyDiskIdString));
		
		return bufferList;
	}
	
	private Block[] getToBeFlushedBlocks(double arrivalTime, int maxBufferedDiskId) {
		// calculate lambda
		double lambda = calculateLambda(arrivalTime);
		
		// get base buffered blocks
		List<Block> baseBuffer = getBaseBuffer(arrivalTime, maxBufferedDiskId);
		
		// calculate spinning disks energy efficiency
		double baseEE = calculateBaseEnergyEfficiency(lambda, baseBuffer); 
		
		// get standby disks buffer chunks
		List<BufferOfADisk> standbyBufferChunks = getStandbyDisksBufferChunks(arrivalTime);
		
		// decide to be spin up disks and get all chunks of them
		List<Block> tempToBeFlushedBlocks = getTobeFlushedBufferBlocks(
				arrivalTime, lambda, baseBuffer, baseEE, standbyBufferChunks);
		
		Block[] fixedTobeFlushedBlocks = 
				extraUniqueBlocks(tempToBeFlushedBlocks, this.cmResponse.getOverflows());
		
		// log to be flushed buffer info
		logger.debug(String.format("To be Flush Buffer Size:%d", fixedTobeFlushedBlocks.length));

		return fixedTobeFlushedBlocks;
	}
	
	private double calculateLambda(double arrivalTime) {
		// lambda is the mean arrival ratio of write accesses (blocks/s)
		double lambda = 
				this.bufferMonitor.getMeanArrivalRateOfWriteAccesses(arrivalTime);
		
		logger.debug(String.format("lambda:%,.4f", lambda));
		return lambda;
	}
	
	private List<Block> getBaseBuffer(double arrivalTime, int maxBufferedDiskId) {
		// get all buffered data chunks of active disks.
		List<Integer> spinningDiskIds = getSpinningDiskIds(arrivalTime);
		List<Block> baseBlocks = new ArrayList<Block>();
		
		// add the max buffered disk id in overflow chunk into spinning disk ids
		// to avoid duplicate extra spinning disks buffer blocks.
		spinningDiskIds.add(maxBufferedDiskId);

		RAPoSDACacheMemoryManager cmm = sm.getCacheMemoryManager();
		RAPoSDADataDiskManager ddm = 
				(RAPoSDADataDiskManager)sm.getDataDiskManager();
		
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

	private double calculateBaseEnergyEfficiency(
			double lambda, List<Block> baseBuffer) {
		
		// set the initial value of results
		int baseBufferSize = baseBuffer.size();
		
		double baseTimeToNextBufferOverflow = baseBufferSize / lambda;
		// minimum energy efficiency of the base.
		double baseEE = 1 / baseTimeToNextBufferOverflow;
		
		logger.debug(String.format("Base Size:%d baseEE:%,.4f", baseBufferSize, baseEE));
		
		return baseEE;
	}
	
	private List<Block> getTobeFlushedBufferBlocks(
			double arrivalTime, double lambda, List<Block> baseBlocks,
			double baseEE, 	List<BufferOfADisk> standbyBufferChunks) {
		
		// energy efficiency of this standby disks and base.
		double tempEE = Double.MAX_VALUE;
		List<Integer> toSpinupDiskIds = new ArrayList<Integer>();
		
		// buffer data to be written to the data disks.
		List<Block> toWriteBuffer = new ArrayList<Block>();
		toWriteBuffer.addAll(baseBlocks);
		
		// sort the buffered data order by descending of its size.
		BufferOfADisk[] sortedBuffers = sortBufferedData(standbyBufferChunks);
		int subsetOfStandbyDisks = 0;
		double spinupPower = Parameter.HDD_SPINUP_ENERGY / Parameter.HDD_SPINUP_TIME;
		
		double minEE = baseEE;
		
		for (int i = sortedBuffers.length - 1; i >= 0; i--) {
			double ttnbo = 0.0; // the time to the next buffer overflow
			ttnbo = (toWriteBuffer.size() + sortedBuffers[i].getSize()) / lambda;
			tempEE = spinupPower * subsetOfStandbyDisks / ttnbo;
			
			if (tempEE < minEE) {
				toWriteBuffer.addAll(sortedBuffers[i].getBlocks());
				minEE = tempEE;
				toSpinupDiskIds.add(sortedBuffers[i].getDiskId());
				subsetOfStandbyDisks++;
			} else {
				break;
			}
		}
		
		// log most energy efficiency
		logger.debug(String.format("mostEE:%,.4f", minEE));
		// log to be spin up disk ids
		logToBeSpinupedDiskIds(arrivalTime, toSpinupDiskIds);

		return toWriteBuffer;
	}
	
	private void logToBeSpinupedDiskIds(
			double arrivalTime, List<Integer> toSpinupDiskIds) {
		
		StringBuffer toSpinupDiskIdString = new StringBuffer();
		
		RAPoSDADataDiskManager ddm = 
				(RAPoSDADataDiskManager)sm.getDataDiskManager();
		
		// return the most energy efficient chunks
		for (Integer diskId : toSpinupDiskIds) {
			if (!ddm.isSpinning(diskId, arrivalTime)) {
				ddm.spinUp(diskId, arrivalTime);
			}
			toSpinupDiskIdString.append(diskId).append(",");
		}

		// log to be spin up disks for flush
		logger.debug(String.format("ToSpinup Disk ID(due to ee calculus):%s", toSpinupDiskIdString));
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
