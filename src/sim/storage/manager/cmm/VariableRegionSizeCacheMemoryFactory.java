package sim.storage.manager.cmm;

import sim.Parameter;
import sim.storage.CacheParameter;

public class VariableRegionSizeCacheMemoryFactory implements CacheMemoryFactory {

	@Override
	public CacheMemory getCacheMemory(
			int id, int numReplica, CacheParameter parameter, int blockSize) {
		VariableRegionSizeCacheMemory cacheMemory =
			new VariableRegionSizeCacheMemory(id, numReplica, parameter, blockSize);
		cacheMemory.setBufferCoefficient(Parameter.CACHE_MEMORY_BUFFER_COEFFICIENT);
		cacheMemory.setUpRegions(numReplica, blockSize);
		return cacheMemory;
	}

}
