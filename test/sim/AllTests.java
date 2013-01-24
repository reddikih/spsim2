package sim;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import sim.storage.DiskCacheTest;
import sim.storage.HardDiskDriveTest;
import sim.storage.manager.cdm.CacheDiskTest;
import sim.storage.manager.cdm.RAPoSDACacheDiskManagerTest;
import sim.storage.manager.cmm.RAPoSDACacheMemoryManagerTest;
import sim.storage.manager.cmm.RegionTest;
import sim.storage.manager.ddm.RAPoSDADataDiskManagerTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	DiskCacheTest.class,
	HardDiskDriveTest.class,
	RegionTest.class,
	RAPoSDACacheMemoryManagerTest.class,
	RAPoSDADataDiskManagerTest.class,
	CacheDiskTest.class,
	RAPoSDACacheDiskManagerTest.class
})

public class AllTests {}

