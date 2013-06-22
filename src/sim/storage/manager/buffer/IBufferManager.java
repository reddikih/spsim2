package sim.storage.manager.buffer;

import sim.Block;
import sim.storage.DiskResponse;

public interface IBufferManager {
	
	public DiskResponse write(Block[] blocks);

}
