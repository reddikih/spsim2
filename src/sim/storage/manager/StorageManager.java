package sim.storage.manager;

import java.math.BigInteger;
import java.util.HashMap;

import sim.Block;
import sim.Request;
import sim.Response;
import sim.storage.manager.ddm.IDataDiskManager;
import sim.storage.util.ReplicaLevel;

public abstract class StorageManager {

	private IDataDiskManager ddm;

	protected int blockSize;
	protected int numReplica;

	protected BigInteger blockNumber = new BigInteger("0");

	public StorageManager(IDataDiskManager ddm) {
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

	protected Block[] createReplicas(Block block) {
		Block[] replicas = new Block[numReplica];
		block.setRepLevel(ReplicaLevel.ZERO);
		block.setOwnerDiskId(block.getPrimaryDiskId());
		replicas[0] = block;
		int repCounter = numReplica;
		for (ReplicaLevel repLevel : ReplicaLevel.values()) {
			repCounter--;
			if (repLevel.equals(ReplicaLevel.ZERO)) continue;
			if (repCounter < 0) break;
			Block replica = new Block(
					nextBlockId(),
					block.getAccessTime(),
					block.getPrimaryDiskId());
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
			BigInteger blockId = nextBlockId();
			blocks[i] = new Block(
					blockId,
					request.getArrvalTime(),
					assignPrimaryDiskId(blockId));
		}
		return blocks;
	}

	protected int assignPrimaryDiskId(BigInteger blockId) {
		BigInteger numDataDisk =
			new BigInteger(String.valueOf(ddm.getNumberOfDataDisks()));
		// TODO try to separate assignor class.
		// There is not only roundrobin favor assign algorithm.
		return (blockId.mod(numDataDisk)).intValue();
	}

	protected int assignOwnerDiskId(
			int primaryDiskId, ReplicaLevel repLevel) {
		return (primaryDiskId + repLevel.getValue()) % ddm.getNumberOfDataDisks();
	}

	protected BigInteger nextBlockId() {
		BigInteger next = new BigInteger(blockNumber.toString());
		blockNumber = blockNumber.add(BigInteger.ONE);
		return next;
	}

	protected void updateArrivalTimeOfBlocks(Block[] blocks, double arrivalTime) {
		for (Block block : blocks) {
			block.setAccessTime(arrivalTime);
		}
	}

	public void register(long requestId, int size) {
		// TODO refactoring. integrate with divideRequest method
		Block[] blocks = null;
		int numBlocks = (int)Math.ceil(size / blockSize);
		if (numBlocks <= 0) numBlocks = 1;
		blocks = new Block[numBlocks];
		for (int i=0; i < numBlocks; i++) {
			BigInteger blockId = nextBlockId();
			blocks[i] = new Block(
					blockId,
					0.0,
					assignPrimaryDiskId(blockId));
		}
		requestMap.put(requestId, blocks);
	}

}
