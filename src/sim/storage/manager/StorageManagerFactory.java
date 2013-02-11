package sim.storage.manager;

import sim.storage.manager.cdm.ICacheDiskManager;
import sim.storage.manager.cmm.ICacheMemoryManager;
import sim.storage.manager.ddm.IDataDiskManager;

public abstract class StorageManagerFactory {

	public static StorageManagerFactory
	getStorageManagerFactory(String factoryName) {
		StorageManagerFactory factory = null;
		try {
			factory = (StorageManagerFactory)Class.forName(factoryName).newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		return factory;
	}

	public StorageManager createStorageManager() {
		ICacheMemoryManager cmm = createCacheMemoryManager();
		ICacheDiskManager cdm = createCacheDiskManager();
		IDataDiskManager ddm = createDataDiskManager();

		StorageManager sm = createStorageManager(cmm, cdm, ddm);
		return sm;
	}

	protected abstract ICacheMemoryManager createCacheMemoryManager();
	protected abstract ICacheDiskManager createCacheDiskManager();
	protected abstract IDataDiskManager createDataDiskManager();

	protected abstract StorageManager createStorageManager(
			ICacheMemoryManager cmm, ICacheDiskManager cdm, IDataDiskManager ddm);
}
