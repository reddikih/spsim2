package sim.storage.manager.buffer;

import sim.storage.manager.StorageManager;

public class EnergyEfficientBufferManagerFactory implements
		IBufferManagerFactory {

	@Override
	public BufferManager createBufferManager(StorageManager sm) {
		EnergyEfficientBufferManager bufferManager = 
				new EnergyEfficientBufferManager(sm);
		return bufferManager;
	}

}
