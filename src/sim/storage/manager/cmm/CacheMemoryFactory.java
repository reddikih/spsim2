package sim.storage.manager.cmm;

import sim.storage.CacheParameter;

public interface CacheMemoryFactory {

	public CacheMemory getCacheMemory(
			int id, int numReplica, CacheParameter parameter, int blockSize);

}
