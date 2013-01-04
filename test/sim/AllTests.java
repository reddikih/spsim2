package sim;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import sim.storage.DiskCacheTest;
import sim.storage.HardDiskDriveTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	DiskCacheTest.class,
	HardDiskDriveTest.class,
})

public class AllTests {}

