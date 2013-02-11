package sim.storage.manager.cmm;

import sim.Block;
import sim.storage.CacheResponse;

public interface ICacheMemoryManager {

	public CacheResponse read(Block block);

	public RAPoSDACacheWriteResponse write(Block block);

	public CacheResponse remove(Block toRemove);

}
