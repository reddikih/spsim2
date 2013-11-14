package sim.storage.manager.cmm;

import sim.Block;
import sim.storage.CacheResponse;

public class MAIDCacheMemoryManager implements ICacheMemoryManager {

    private CacheMemory cacheMemory;

    public MAIDCacheMemoryManager(CacheMemory cacheMemory) {
        this.cacheMemory = cacheMemory;
    }

    @Override
    public CacheResponse read(Block block) {
        CacheResponse result;
        Block retrieveBlock = new Block(
                block.getId(),
                block.getAccessTime(),
                block.getPrimaryDiskId());
        result = cacheMemory.read(retrieveBlock);

        assert result != null
                : "the return value of cache memory read is null. blockID:" + block.getId();

        return result;
    }

    @Override
    public RAPoSDACacheWriteResponse write(Block block) {
        return cacheMemory.write(block);
    }

    @Override
    public CacheResponse remove(Block toRemove) {
        return cacheMemory.remove(toRemove);
    }
}
