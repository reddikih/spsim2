package sim.storage.manager.cdm;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import sim.Block;
import sim.statistics.RAPoSDAStats;
import sim.storage.CacheResponse;
import sim.storage.DiskResponse;
import sim.storage.util.RequestType;

public class RAPoSDACacheDiskManager implements ICacheDiskManager {

	private int numberOfCacheDisks;
	/**
	 * key: cache disk id
	 * value: cache disk object
	 */
	private HashMap<Integer, CacheDisk> cacheDisks;

	/**
	 * key: block id
	 * value: cache disk id
	 */
	private HashMap<BigInteger, Integer> block2cdMap;

	private int lastAssignedId;

	public RAPoSDACacheDiskManager(
			int numberOfCacheDisks, HashMap<Integer, CacheDisk> cacheDiskMap) {
		this.numberOfCacheDisks = numberOfCacheDisks;
		this.cacheDisks = cacheDiskMap;
		this.block2cdMap = new HashMap<BigInteger, Integer>();
	}

	@Override
	public DiskResponse write(Block[] blocks) {
		if (blocks == null)
			throw new IllegalArgumentException("blocks should not be null.");

		CacheDisk cd;
		CacheResponse cResp;
		List<Block> writtenBlocks = new ArrayList<Block>();
		double respTime = Double.MIN_VALUE;

		for (Block b : blocks) {
			int cdId;
			if (block2cdMap.containsKey(b.getId())) {
				// update write request
				cdId = block2cdMap.get(b.getId());
			} else {
				// newly write
				cdId = assignIdByRoundRobin();
				block2cdMap.put(b.getId(), cdId);
			}
			cd = cacheDisks.get(cdId);

			cResp = cd.write(b);

			// When a block replaced in the cache disk,
			// then delete the corresponding entry of block2cdMap.
			if (!cResp.getResult().equals(b))
				deleteReplacedBlockFromMap(cResp.getResult());

			respTime =
				respTime < cResp.getResponseTime()
				? cResp.getResponseTime() : respTime;
			writtenBlocks.add(cResp.getResult());
		}
		return new DiskResponse(respTime, writtenBlocks.toArray(new Block[0]));
	}

	private void deleteReplacedBlockFromMap(Block block) {
		block2cdMap.remove(block.getId());
	}

	@Override
	public CacheResponse read(Block block) {
		if (block == null)
			throw new IllegalArgumentException("block should not be null.");

		if (!block2cdMap.containsKey(block.getId())) {
			// record statistics cache disk read
			RAPoSDAStats.incrementCacheDiskAccessCount(RequestType.READ, false);
			return new CacheResponse(Double.MAX_VALUE, Block.NULL);
		}

		CacheDisk cd = cacheDisks.get(block2cdMap.get(block.getId()));
		assert cd != null;

		return cd.read(block);
	}

	private int assignIdByRoundRobin() {
		assert 0 <= lastAssignedId && lastAssignedId <= numberOfCacheDisks;
		lastAssignedId = lastAssignedId % numberOfCacheDisks;
		return lastAssignedId++;
	}

	@Override
	public void close(double closeTime) {
		Collection<CacheDisk> cds = cacheDisks.values();
		for (CacheDisk cd : cds) {
			cd.close(closeTime);
		}
	}

}
