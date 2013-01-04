package sim.storage;

import java.math.BigInteger;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import sim.Block;
import sim.Parameter;
import sim.storage.util.ReplicaLevel;

@RunWith(JUnit4.class)
public class HardDiskDriveTest {

	private HDDParameter withoutDiskCacheParam;
	private HDDParameter withDiskCacheParam;

	@Before
	public void setUp() {
		withoutDiskCacheParam = new HDDParameter(
				5, //size
				Parameter.HDD_NUMBER_OF_PLATTER,
				Parameter.HDD_RPM,
				Parameter.HDD_CACHE_SIZE,
				Parameter.HDD_TRANSFER_RATE,
				Parameter.HDD_SECTORS_PER_TRACK,
				Parameter.HDD_FULL_STROKE_SEEK_TIME,
				Parameter.HDD_HEAD_SWITCH_OVERHEAD,
				Parameter.HDD_COMMAND_OVERHEAD,
				Parameter.HDD_ACTIVE_POWER,
				Parameter.HDD_IDLE_POWER,
				Parameter.HDD_STANDBY_POWER,
				Parameter.HDD_SPINDOWN_ENERGY,
				Parameter.HDD_SPINUP_ENERGY,
				Parameter.HDD_SPINDOWN_TIME,
				Parameter.HDD_SPINUP_TIME
		);

		withDiskCacheParam = new HDDParameter(
				5, // disk capacity
				Parameter.HDD_NUMBER_OF_PLATTER,
				Parameter.HDD_RPM,
				3, // disk cache capacity
				Parameter.HDD_TRANSFER_RATE,
				Parameter.HDD_SECTORS_PER_TRACK,
				Parameter.HDD_FULL_STROKE_SEEK_TIME,
				Parameter.HDD_HEAD_SWITCH_OVERHEAD,
				Parameter.HDD_COMMAND_OVERHEAD,
				Parameter.HDD_ACTIVE_POWER,
				Parameter.HDD_IDLE_POWER,
				Parameter.HDD_STANDBY_POWER,
				Parameter.HDD_SPINDOWN_ENERGY,
				Parameter.HDD_SPINUP_ENERGY,
				Parameter.HDD_SPINDOWN_TIME,
				Parameter.HDD_SPINUP_TIME
		);
	}

	@Test
	public void writeWithoutDiskCache() {
		HardDiskDrive hdd = new HardDiskDrive(0, withoutDiskCacheParam);

		Block block0 = new Block(new BigInteger(String.valueOf(0)), ReplicaLevel.ZERO, 0.0);
		Block block1 = new Block(new BigInteger(String.valueOf(1)), ReplicaLevel.ZERO, 0.1);
		Block block2 = new Block(new BigInteger(String.valueOf(2)), ReplicaLevel.ZERO, 0.2);

		double response = -1;

		Block[] blocks = {block0};
		response = hdd.write(blocks);
		assertThat((response < 0.1 && response > 0.001), is(true));

		blocks = new Block[]{block0, block1, block2};
		response = hdd.write(blocks);
		assertThat((response < 0.1 && response > 0.001), is(true));
	}
}
