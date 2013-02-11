package sim.storage.manager.cmm;

import sim.Block;
import sim.storage.CacheResponse;

public class NormalCacheMemoryManager implements ICacheMemoryManager {

//	private int numReplica;
	private CacheMemory cacheMemory;

//	public NormalCacheMemoryManager(CacheMemory cacheMemory, int numReplica) {
	public NormalCacheMemoryManager(CacheMemory cacheMemory) {
		this.cacheMemory = cacheMemory;
//		this.numReplica = numReplica;
	}

	@Override
	public CacheResponse read(Block block) {
		CacheResponse result = null;
		Block retrieveBlock = new Block(
				block.getId(),
				block.getAccessTime(),
				block.getPrimaryDiskId());
		result = cacheMemory.read(retrieveBlock);

		assert result != null
		: "cache memeory read return value is null. blockId:" + block.getId();

		return result;
	}

	@Override
	public RAPoSDACacheWriteResponse write(Block block) {
		RAPoSDACacheWriteResponse response;
		response = cacheMemory.write(block);
		return response;
	}

	@Override
	public CacheResponse remove(Block toRemove) {
		return cacheMemory.remove(toRemove);
	}

}
