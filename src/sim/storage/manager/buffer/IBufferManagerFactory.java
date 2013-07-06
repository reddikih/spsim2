package sim.storage.manager.buffer;

import sim.storage.manager.StorageManager;

public interface IBufferManagerFactory {
	
	public BufferManager createBufferManager(StorageManager sm);

}
