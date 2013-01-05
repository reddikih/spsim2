package sim;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import sim.storage.DiskCacheTest;
import sim.storage.HardDiskDriveTest;
import sim.storage.manager.cmm.RegionTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	DiskCacheTest.class,
	HardDiskDriveTest.class,
	RegionTest.class,
})

public class AllTests {}

