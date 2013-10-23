package sim.storage.manager.cmm;

import sim.storage.CacheParameter;

public class SharedRegionsCacheMemoryFactory implements CacheMemoryFactory {

	@Override
	public CacheMemory getCacheMemory(int id, int numReplica,
			CacheParameter parameter, int blockSize) {
		SharedRegionsCacheMemory cacheMemory = 
				new SharedRegionsCacheMemory(id, numReplica, parameter, blockSize);
		cacheMemory.setUpRegions(numReplica, blockSize);
		return cacheMemory;
	}

}
