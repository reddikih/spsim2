package sim.storage.manager.buffer;

import sim.storage.manager.StorageManager;

public class RAPoSDABufferManagerFactory implements IBufferManagerFactory {

	@Override
	public IBufferManager createBufferManager(StorageManager sm) {
		RAPoSDABufferManager bufferManager = new RAPoSDABufferManager(sm);
		return bufferManager;
	}

}
