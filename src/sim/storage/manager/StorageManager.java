package sim.storage.manager;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sim.Block;
import sim.Request;
import sim.Response;
import sim.storage.manager.cdm.ICacheDiskManager;
import sim.storage.manager.cmm.ICacheMemoryManager;
import sim.storage.manager.ddm.IDataDiskManager;
import sim.storage.util.ReplicaLevel;

public abstract class StorageManager {

	protected ICacheMemoryManager cmm;
	protected ICacheDiskManager cdm;
	protected IDataDiskManager ddm;

	protected int blockSize;
	protected int numReplica;

	protected long sequenceNumber = 1L; // 1 origin
	
	private static Logger mappinglogger = LoggerFactory.getLogger("REQUEST_BLOCK_MAPPING");

	public StorageManager(
			ICacheMemoryManager cmm,
			ICacheDiskManager cdm,
			IDataDiskManager ddm) {
		this.cmm = cmm;
		this.cdm = cdm;
		this.ddm = ddm;
	}

	/**
	 * A map between client request and corresponding blocks.
	 * key; request key
	 * value: corresponding blocks
	 */
	protected HashMap<Long, Block[]> requestMap;

	abstract public Response read(Request request);

	abstract public Response write(Request request);

	abstract public void close(double closeTime);

	
	public Block[] createReplicas(Block primaryBlock) {
		Block[] replicas = new Block[numReplica];
		primaryBlock.setRepLevel(ReplicaLevel.ZERO);
		primaryBlock.setOwnerDiskId(primaryBlock.getPrimaryDiskId());
		replicas[0] = primaryBlock;
		int repCounter = numReplica;
		for (ReplicaLevel repLevel : ReplicaLevel.values()) {
			repCounter--;
			if (repLevel.equals(ReplicaLevel.ZERO)) continue;
			if (repCounter < 0) break;
			Block replica = new Block(
					primaryBlock.getId() + repLevel.getValue(),
					primaryBlock.getAccessTime(),
					primaryBlock.getPrimaryDiskId());
			replica.setRepLevel(repLevel);
			replica.setOwnerDiskId(
					assignOwnerDiskId(
							replica.getPrimaryDiskId(),
							replica.getRepLevel()));
			replicas[repLevel.getValue()] = replica;
		}
		return replicas;
	}

	protected Block[] divideRequest(Request request) {
		Block[] blocks = null;
		int numBlocks = (int)Math.ceil(request.getSize() / blockSize);
		assert numBlocks > 0;
		blocks = new Block[numBlocks];
		for (int i=0; i < numBlocks; i++) {
			long blockId = nextBlockId();
			blocks[i] = new Block(
					blockId,
					request.getArrvalTime(),
					assignPrimaryDiskId(blockId));
		}
		return blocks;
	}

	protected int assignPrimaryDiskId(long blockId) {
		long numDataDisk = ddm.getNumberOfDataDisks();
		// TODO try to separate assignor class.
		// There is not only roundrobin favor assign algorithm.
		return (int)((blockId / this.numReplica) % numDataDisk);
	}

	protected int assignOwnerDiskId(
			int primaryDiskId, ReplicaLevel repLevel) {
		return (primaryDiskId + repLevel.getValue()) % ddm.getNumberOfDataDisks();
	}

	protected long nextBlockId() {
		return (this.sequenceNumber++ - 1) * this.numReplica;
	}

	public void updateArrivalTimeOfBlocks(Block[] blocks, double arrivalTime) {
		for (Block block : blocks) {
			block.setAccessTime(arrivalTime);
		}
	}

	public void register(long requestId, int size) {
		// TODO refactoring. integrate with divideRequest method
		StringBuffer blocksBuf = new StringBuffer();
		Block[] blocks = null;
		int numBlocks = (int)Math.ceil(size / blockSize);
		if (numBlocks <= 0) numBlocks = 1;
		blocks = new Block[numBlocks];
		for (int i=0; i < numBlocks; i++) {
			long blockId = nextBlockId();
			blocks[i] = new Block(
					blockId,
					0.0,
					assignPrimaryDiskId(blockId));
			blocksBuf.append(blockId).append(",");
		}
		requestMap.put(requestId, blocks);
		
		// logging the mapping information
		mappinglogger.trace(
				String.format(
						"RequestId:%d blockIds:%s primaryDiskId:%d",
						requestId, blocksBuf.toString(), blocks[0].getPrimaryDiskId()));
	}

	public IDataDiskManager getDataDiskManager() {
		return this.ddm;
	}
}
