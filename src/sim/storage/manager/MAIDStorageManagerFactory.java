package sim.storage.manager;

import sim.Parameter;
import sim.storage.CacheParameter;
import sim.storage.HDDParameter;
import sim.storage.manager.cdm.CacheDisk;
import sim.storage.manager.cdm.ICacheDiskManager;
import sim.storage.manager.cdm.MAIDCacheDiskManager;
import sim.storage.manager.cmm.CacheMemory;
import sim.storage.manager.cmm.CacheMemoryFactory;
import sim.storage.manager.cmm.ICacheMemoryManager;
import sim.storage.manager.cmm.MAIDCacheMemoryManager;
import sim.storage.manager.ddm.DataDisk;
import sim.storage.manager.ddm.IDataDiskManager;
import sim.storage.manager.ddm.MAIDDataDiskManager;
import sim.storage.state.DiskStateParameter;
import sim.storage.state.WithSleepDiskStateManager;
import sim.storage.state.WithoutSleepDiskStateManager;

import java.util.HashMap;

public class MAIDStorageManagerFactory extends StorageManagerFactory {

    @Override
    protected ICacheMemoryManager createCacheMemoryManager() {
        CacheParameter param = new CacheParameter(
                Parameter.CACHE_MEMORY_THRESHOLD,
                Parameter.CACHE_MEMORY_SIZE,
                Parameter.CACHE_MEMORY_LATENCY
        );

        CacheMemory cm = null;
        try {
            CacheMemoryFactory factory =
                    (CacheMemoryFactory) Class.forName(
                            Parameter.CACHE_MEMORY_FACTORY).newInstance();
            cm = factory.getCacheMemory(0, 1, param, Parameter.BLOCK_SIZE);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        return new MAIDCacheMemoryManager(cm);
    }

    @Override
    protected ICacheDiskManager createCacheDiskManager() {
        int numcd = Parameter.NUMBER_OF_CACHE_DISKS;
        HashMap<Integer, CacheDisk> cacheDisks = new HashMap<Integer, CacheDisk>();

        HDDParameter param = new HDDParameter(
                Parameter.HDD_SIZE,
                Parameter.HDD_NUMBER_OF_PLATTER,
                Parameter.HDD_RPM,
                Parameter.HDD_CACHE_SIZE,
                Parameter.HDD_TRANSFER_RATE,
                Parameter.HDD_SECTORS_PER_TRACK,
                Parameter.HDD_FULL_STROKE_SEEK_TIME,
                Parameter.HDD_HEAD_SWITCH_OVERHEAD,
                Parameter.HDD_COMMAND_OVERHEAD
        );

        WithoutSleepDiskStateManager cdstm =
                new WithoutSleepDiskStateManager(
                        new DiskStateParameter(
                                Parameter.HDD_ACTIVE_POWER,
                                Parameter.HDD_IDLE_POWER,
                                Parameter.HDD_STANDBY_POWER,
                                Parameter.HDD_SPINDOWN_ENERGY,
                                Parameter.HDD_SPINDOWN_ENERGY,
                                Parameter.HDD_SPINDOWN_TIME,
                                Parameter.HDD_SPINUP_TIME
                        )
                );

        for (int i = 0; i < numcd; i++) {
            CacheDisk cd = new CacheDisk(i, Parameter.BLOCK_SIZE, param, cdstm);
            cacheDisks.put(i, cd);
        }

        return new MAIDCacheDiskManager(numcd, cacheDisks);
    }

    @Override
    protected IDataDiskManager createDataDiskManager() {
        int numdd = Parameter.NUMBER_OF_DISKS_PER_CACHE_GROUP;
        int numRep = Parameter.NUMBER_OF_REPLICA;
        HashMap<Integer, DataDisk> dataDisks = new HashMap<Integer, DataDisk>();

        HDDParameter param = new HDDParameter(
                Parameter.HDD_SIZE,
                Parameter.HDD_NUMBER_OF_PLATTER,
                Parameter.HDD_RPM,
                Parameter.HDD_CACHE_SIZE,
                Parameter.HDD_TRANSFER_RATE,
                Parameter.HDD_SECTORS_PER_TRACK,
                Parameter.HDD_FULL_STROKE_SEEK_TIME,
                Parameter.HDD_HEAD_SWITCH_OVERHEAD,
                Parameter.HDD_COMMAND_OVERHEAD
        );

        WithSleepDiskStateManager ddstm =
                new WithSleepDiskStateManager(
                        Parameter.SPINDOWN_THRESHOLD,
                        new DiskStateParameter(
                                Parameter.HDD_ACTIVE_POWER,
                                Parameter.HDD_IDLE_POWER,
                                Parameter.HDD_STANDBY_POWER,
                                Parameter.HDD_SPINDOWN_ENERGY,
                                Parameter.HDD_SPINDOWN_ENERGY,
                                Parameter.HDD_SPINDOWN_TIME,
                                Parameter.HDD_SPINUP_TIME
                        )
                );

        for (int i = 0; i < numdd; i++) {
            DataDisk dd = new DataDisk(i, param, ddstm);
            dataDisks.put(i, dd);
        }
        return new MAIDDataDiskManager(numdd, numRep, dataDisks);
    }

    @Override
    protected StorageManager createStorageManager(
            ICacheMemoryManager cmm,
            ICacheDiskManager cdm,
            IDataDiskManager ddm) {

        int blockSize = Parameter.BLOCK_SIZE;
        int numRep = Parameter.NUMBER_OF_REPLICA;

        assert numRep > 0 : "number of replica parameter should greater than 0";

        return new MAIDStorageManager(
                (MAIDCacheMemoryManager) cmm,
                (MAIDCacheDiskManager) cdm,
                (MAIDDataDiskManager) ddm,
                blockSize, numRep);
    }
}
