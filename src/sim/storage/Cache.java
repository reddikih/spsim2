package sim.storage;

import sim.Block;

public interface Cache {

	public CacheResponse read(Block block);

	public CacheResponse write(Block block);

}
