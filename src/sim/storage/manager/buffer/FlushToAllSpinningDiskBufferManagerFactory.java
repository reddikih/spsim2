package sim.storage.manager.buffer;

import sim.storage.manager.StorageManager;

public class FlushToAllSpinningDiskBufferManagerFactory implements
		IBufferManagerFactory {

	@Override
	public BufferManager createBufferManager(StorageManager sm) {
		FlushToAllSpinningDiskBufferManager bufferManager = 
				new FlushToAllSpinningDiskBufferManager(sm);
		return bufferManager;
	}

}
