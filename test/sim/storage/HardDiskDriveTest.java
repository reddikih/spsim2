package sim.storage;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import sim.Block;
import sim.Parameter;
import sim.storage.util.ReplicaLevel;

@RunWith(JUnit4.class)
public class HardDiskDriveTest {

	private HDDParameter withoutDiskCacheParam;
	private HDDParameter withDiskCacheParam;
	private double oneBlockResponse;

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
				Parameter.HDD_COMMAND_OVERHEAD
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
				Parameter.HDD_COMMAND_OVERHEAD
		);

		HardDiskDrive hdd = new HardDiskDrive(0, withoutDiskCacheParam);
		Block block = new Block(0, ReplicaLevel.ZERO, 0.0, 0);
		Block[] blocks = {block};
		oneBlockResponse = hdd.write(blocks);
	}

	@Test
	public void writeWithoutDiskCache() {
		HardDiskDrive hdd = new HardDiskDrive(0, withoutDiskCacheParam);

		Block block0 = new Block(0, ReplicaLevel.ZERO, 0.0, 0);
		Block[] blocks = {block0};
		oneBlockResponse = hdd.write(blocks);
		assertThat((oneBlockResponse < 0.01 && oneBlockResponse > 0.001), is(true));

		blocks = new Block[0];
		double response = hdd.write(blocks);
		assertThat(response, is(Double.MIN_VALUE));
	}

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void assertionErrorOccurenceInWrite() {
		HardDiskDrive hdd = new HardDiskDrive(0, withoutDiskCacheParam);

		Block block1 = new Block(1, ReplicaLevel.ZERO, 1.0, 0);
		Block block2 = new Block(2, ReplicaLevel.ZERO, 2.0, 0);
		Block[] blocks = new Block[]{block1, block2};

		thrown.expect(AssertionError.class);
		hdd.write(blocks);
	}

	@Test
	public void sequencialWriteWithoutDiskCache() {
		HardDiskDrive hdd = new HardDiskDrive(0, withoutDiskCacheParam);
		double response = -1;
		Block[] blocks = null;

		// sequencial write requests.
		Block block0 = new Block(0, ReplicaLevel.ZERO, 0.0, 0);
		Block block1 = new Block(1, ReplicaLevel.ZERO, 0.0 + oneBlockResponse, 0);
		Block block2 = new Block(2, ReplicaLevel.ZERO, 0.0 + oneBlockResponse * 2, 0);

		blocks = new Block[]{block0};
		response = hdd.write(blocks);
		assertThat(response, is(oneBlockResponse));

		blocks = new Block[]{block1};
		response = hdd.write(blocks);
		assertThat(response, is(oneBlockResponse));

		blocks = new Block[]{block2};
		response = hdd.write(blocks);
		assertThat(response, is(oneBlockResponse));
	}

	@Test
	public void simultaneousWriteWithoutDiskCache() {
		HardDiskDrive hdd = new HardDiskDrive(0, withoutDiskCacheParam);
		double response = -1;
		Block[] blocks = null;

		// sequencial write requests.
		Block block0 = new Block(0, ReplicaLevel.ZERO, 0.0, 0);
		Block block1 = new Block(1, ReplicaLevel.ZERO, 0.0, 0);

		blocks = new Block[]{block0, block1};
		response = hdd.write(blocks);
		assertThat(response, is(oneBlockResponse * 2));
	}

	@Test
	public void readWithoutDiskCache() {
		HardDiskDrive hdd = new HardDiskDrive(0, withoutDiskCacheParam);
		double response = -1;
		Block[] blocks = null;

		Block block0 = new Block(0, ReplicaLevel.ZERO, 0.0, 0);
		blocks = new Block[]{block0};
		hdd.write(blocks);

		block0.setAccessTime(0.1);
		response = hdd.read(blocks);
		assertThat(response, is(oneBlockResponse));

		// sequencial read requests.
		Block block1 = new Block(1, ReplicaLevel.ZERO, 1.0, 0);
		Block block2 = new Block(2, ReplicaLevel.ZERO, 1.0, 0);

		blocks = new Block[]{block1, block2};
		int intRes = (int)(hdd.read(blocks) * 1000000000);
		int expectedResp = (int)(oneBlockResponse * 2 * 1000000000);
		assertThat(intRes, is(expectedResp));
	}
}
