package sim.storage.manager.cdm;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import sim.Block;
import sim.storage.Cache;
import sim.storage.CacheResponse;
import sim.storage.HDDParameter;
import sim.storage.HardDiskDrive;
import sim.storage.state.CacheDiskStateManager;
import sim.storage.util.DiskState;

public class CacheDisk extends HardDiskDrive implements Cache {

	private CacheDiskStateManager stm;
	private long maxEntries;

	private HashMap<BigInteger, Block> caches;
	private TreeMap<Double, BigInteger> usedKeys;


	public CacheDisk(
			int id,
			int blockSize,
			HDDParameter parameter,
			CacheDiskStateManager stm) {
		super(id, parameter);
		init(blockSize, stm);
	}

	private void init(int blockSize, CacheDiskStateManager stm) {
		assert stm != null;
		this.stm = stm;
		this.maxEntries =
			(int)Math.floor((double)parameter.getHddSize() / blockSize);
		this.caches = new HashMap<BigInteger, Block>();
		this.usedKeys = new TreeMap<Double, BigInteger>();
	}

	@Override
	public CacheResponse read(Block block) {
		Block retrieved = getEntry(block);
		double response = Double.MAX_VALUE;
		if (!Block.NULL.equals(retrieved)) {
			response = actualRead(new Block[]{retrieved});
		}
		return new CacheResponse(response, retrieved);
	}

	private double actualRead(Block[] blocks) {
		double arrivalTime = blocks[0].getAccessTime();
		double latency = stm.stateUpdate(arrivalTime, lastIdleStartTime);
		double responseTime = super.read(blocks);

		arrivalTime += latency;

		stm.postStateUpdate(DiskState.ACTIVE, arrivalTime, responseTime);
		return responseTime;
	}

	private Block getEntry(Block block) {
		Block result = Block.NULL;
		BigInteger blockId = block.getId();
		if (caches.containsKey(blockId)) {
			Block temp = caches.get(blockId);
			if (temp.getAccessTime() <= block.getAccessTime()) {
				result = temp;
				usedKeys.remove(result.getAccessTime());
				usedKeys.put(block.getAccessTime(), blockId);
				result.setAccessTime(block.getAccessTime());
			}
		}
		return result;
	}

	@Override
	public CacheResponse write(Block block) {
		Block result = getEntry(block);
		if (Block.NULL.equals(result)) {
			if (caches.size() < maxEntries) {
				addEntry(block);
				result = block;
			} else {
				result = replaceEntry(block);
			}
		}
		result.setAccessTime(block.getAccessTime());
		double diskTime = actualWrite(new Block[]{result});
		return new CacheResponse(diskTime, result);
	}

	private double actualWrite(Block[] blocks) {
		double arrivalTime = blocks[0].getAccessTime();
		double latency = stm.stateUpdate(arrivalTime, lastIdleStartTime);
		double responseTime = super.write(blocks);

		arrivalTime += latency;

		stm.postStateUpdate(DiskState.ACTIVE, arrivalTime, responseTime);
		return responseTime;
	}

	private void addEntry(Block block) {
		caches.put(block.getId(), block);
		usedKeys.put(block.getAccessTime(), block.getId());
	}

	private Block replaceEntry(Block block) {
		Block removed = null;
		Map.Entry<Double, BigInteger> lruEntry = usedKeys.pollFirstEntry();
		removed = caches.remove(lruEntry.getValue());
		addEntry(block);
		return removed;
	}

}
