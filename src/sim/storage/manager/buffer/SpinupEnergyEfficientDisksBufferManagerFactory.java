package sim.storage.manager.buffer;

import sim.storage.manager.StorageManager;

public class SpinupEnergyEfficientDisksBufferManagerFactory implements
		IBufferManagerFactory {

	@Override
	public BufferManager createBufferManager(StorageManager sm) {
		SpinupEnergyEfficientDisksBufferManager bufferManager =
				new SpinupEnergyEfficientDisksBufferManager(sm);
		return bufferManager;
	}

}
