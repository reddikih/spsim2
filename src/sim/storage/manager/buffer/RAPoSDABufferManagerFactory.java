package sim.storage.manager.buffer;

import sim.storage.manager.StorageManager;

public class RAPoSDABufferManagerFactory implements IBufferManagerFactory {

	@Override
	public BufferManager createBufferManager(StorageManager sm) {
		RAPoSDABufferManager bufferManager = new RAPoSDABufferManager(sm);
		return bufferManager;
	}

}