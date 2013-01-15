package sim.storage.manager.cdm;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.math.BigInteger;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import sim.Block;
import sim.Parameter;
import sim.storage.CacheResponse;
import sim.storage.HDDParameter;
import sim.storage.HardDiskDrive;
import sim.storage.state.CacheDiskStateManager;
import sim.storage.state.DiskStateParameter;

@RunWith(JUnit4.class)
public class CacheDiskTest {

	private static double aBlockResp;

	@BeforeClass
	public static void setUp() {
		HardDiskDrive hdd = new HardDiskDrive(0, getHDDParameter());
		aBlockResp = hdd.write(
				new Block[]{new Block(new BigInteger("0"), 0.0, 0)});
	}


	private static HDDParameter getHDDParameter() {
		return new HDDParameter(
				10,
				Parameter.HDD_NUMBER_OF_PLATTER,
				Parameter.HDD_RPM,
				Parameter.HDD_CACHE_SIZE,
				Parameter.HDD_TRANSFER_RATE,
				Parameter.HDD_SECTORS_PER_TRACK,
				Parameter.HDD_FULL_STROKE_SEEK_TIME,
				Parameter.HDD_HEAD_SWITCH_OVERHEAD,
				Parameter.HDD_COMMAND_OVERHEAD);
	}

	private static CacheDiskStateManager getStateManager() {
		return new CacheDiskStateManager(
				 new DiskStateParameter(
							Parameter.HDD_ACTIVE_POWER,
							Parameter.HDD_IDLE_POWER,
							Parameter.HDD_STANDBY_POWER,
							Parameter.HDD_SPINDOWN_ENERGY,
							Parameter.HDD_SPINUP_ENERGY,
							Parameter.HDD_SPINDOWN_TIME,
							Parameter.HDD_SPINUP_TIME));
	}

	@Test
	public void readABlockBeforeCached() {
		CacheDisk cd = new CacheDisk(0, 1, getHDDParameter(), getStateManager());

		Block block0 = new Block(new BigInteger("0"), 10.0, 0);
		Block block1 = new Block(new BigInteger("0"), 5.0, 0);

		CacheResponse response = null;

		response = cd.write(block0);
		assertThat(response.getResponseTime(), is(aBlockResp));
		assertThat(response.getResult(), is(block0));

		response = cd.read(block1);
		assertThat(response.getResponseTime(), is(Double.MAX_VALUE));
		assertThat(response.getResult(), is(Block.NULL));
	}

}
