package sim.storage.manager.cdm;

import sim.Block;
import sim.storage.CacheResponse;
import sim.storage.DiskResponse;

public interface ICacheDiskManager {

	public DiskResponse write(Block[] blocks);

	public CacheResponse read(Block block);

	public void close(double closeTime);
}
