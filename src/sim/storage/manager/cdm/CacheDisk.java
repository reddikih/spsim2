package sim.storage.manager.cdm;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import sim.Block;
import sim.statistics.RAPoSDAStats;
import sim.storage.Cache;
import sim.storage.CacheResponse;
import sim.storage.HDDParameter;
import sim.storage.HardDiskDrive;
import sim.storage.state.WithoutSleepDiskStateManager;
import sim.storage.util.DiskState;
import sim.storage.util.RequestType;

public class CacheDisk extends HardDiskDrive implements Cache {

	private WithoutSleepDiskStateManager stm;
	private long maxEntries;

	private HashMap<BigInteger, Block> caches;
	private TreeMap<Double, BigInteger> usedKeys;


	public CacheDisk(
			int id,
			int blockSize,
			HDDParameter parameter,
			WithoutSleepDiskStateManager stm) {
		super(id, parameter);
		init(blockSize, stm);
	}

	private void init(int blockSize, WithoutSleepDiskStateManager stm) {
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

		// record statistics cache disk read
		RAPoSDAStats.incrementCacheDiskAccessCount(
				RequestType.READ, !(Block.NULL.equals(retrieved)));

		return new CacheResponse(response, retrieved);
	}

	private double actualRead(Block[] blocks) {
		double arrivalTime = blocks[0].getAccessTime();
		double latency = stm.stateUpdate(this, arrivalTime, lastIdleStartTime);
		double responseTime = super.read(blocks);

		stm.postStateUpdate(
				this,
				DiskState.ACTIVE,
				arrivalTime + latency,
				arrivalTime + responseTime);

		// log access count
		RAPoSDAStats.addCacheDiskResponseTime(responseTime);

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

		// record statistics cache disk write
		RAPoSDAStats.incrementCacheDiskAccessCount(
				RequestType.WRITE, result.equals(block));

		return new CacheResponse(diskTime, result);
	}

	private double actualWrite(Block[] blocks) {
		double arrivalTime = blocks[0].getAccessTime();
		double latency = stm.stateUpdate(this, arrivalTime, lastIdleStartTime);
		double responseTime = super.write(blocks);

		stm.postStateUpdate(
				this,
				DiskState.ACTIVE,
				arrivalTime + latency,
				arrivalTime + responseTime);

		// log access count
		RAPoSDAStats.addCacheDiskResponseTime(responseTime);

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

	public void close(double closeTime) {
		stm.stateUpdate(this, closeTime, lastIdleStartTime);
	}

}
