package sim.storage.manager.cmm;

import sim.storage.CacheParameter;

public class FixedRegionSizeCacheMemoryFactory implements CacheMemoryFactory {

	@Override
	public CacheMemory getCacheMemory(
			int id, int numReplica, CacheParameter parameter, int blockSize) {
		FixedRegionSizeCacheMemory cacheMemory =
			new FixedRegionSizeCacheMemory(id, numReplica, parameter, blockSize);
		cacheMemory.setUpRegions(numReplica, blockSize);
		return cacheMemory;
	}

}
