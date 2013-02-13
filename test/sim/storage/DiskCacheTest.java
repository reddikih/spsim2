package sim.storage;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import sim.Block;
import sim.storage.util.ReplicaLevel;

@RunWith(JUnit4.class)
public class DiskCacheTest {

	private final static int BLOCK_SIZE = 1;
	private DiskCache diskCache;

	@Before
	public void setUp() {
		CacheParameter param = new CacheParameter(1.0, 4, 0.0001);
		diskCache = new DiskCache(0, param, BLOCK_SIZE);
	}

	@Test
	public void writeTest() {
		Block block0 = new Block(0, ReplicaLevel.ZERO, 0.001, 0);
		CacheResponse response = diskCache.write(block0);
		assertThat(response.getResponseTime(), is(0.0001));
		assertThat(response.getResult(), is(block0));

		Block block1 = new Block(1, ReplicaLevel.ZERO, 0.002, 0);
		response = diskCache.write(block1);
		assertThat(response.getResult(), is(block1));

		Block block2 = new Block(2, ReplicaLevel.ZERO, 0.003, 0);
		response = diskCache.write(block2);
		assertThat(response.getResult(), is(block2));

		Block block3 = new Block(3, ReplicaLevel.ZERO, 0.004, 0);
		response = diskCache.write(block3);
		assertThat(response.getResult(), is(block3));

		// overflow check
		Block block4 = new Block(4, ReplicaLevel.ZERO, 0.0019, 0);
		response = diskCache.write(block4);
		assertThat(response.getResult(), is(block0));


		// arrival time consistency check.
		Block block5 = new Block(5, ReplicaLevel.ZERO, 0.005, 0);
		response = diskCache.write(block5);
		assertThat(response.getResult(), is(block4));
		assertThat(response.getResponseTime(), is(0.0001));
	}

	@Test
	public void writeTheSameBlock() {
		CacheParameter param = new CacheParameter(1.0, 3, 0.0001);
		diskCache = new DiskCache(0, param, BLOCK_SIZE);

		CacheResponse response;

		Block block0 = new Block(0, ReplicaLevel.ZERO, 0.001, 0);
		response = diskCache.write(block0);

		Block block1 = new Block(1, ReplicaLevel.ZERO, 0.002, 0);
		response = diskCache.write(block1);

		block0 = new Block(0, ReplicaLevel.ZERO, 0.003, 0);
		response = diskCache.write(block0);

		// block2 can be written on cache without replace.
		Block block2 = new Block(2, ReplicaLevel.ZERO, 0.004, 0);
		response = diskCache.write(block2);
		assertThat(response.getResult(), is(block2));

		// In this time, block1 is replaced because of memory size limit.
		Block block3 = new Block(3, ReplicaLevel.ZERO, 0.005, 0);
		response = diskCache.write(block3);
		assertThat(response.getResult(), is(block1));
		assertThat(response.getResponseTime(), is(0.0001));
	}

	@Test
	public void blockEquality() {
		CacheParameter param = new CacheParameter(1.0, 1, 0.0001);
		diskCache = new DiskCache(0, param, BLOCK_SIZE);

		CacheResponse response;

		Block block0 = new Block(0, ReplicaLevel.ZERO, 0.000, 0);
		response = diskCache.write(block0);
		assertThat(response.getResult(), is(block0));

		Block theSameBlock = new Block(0, ReplicaLevel.ZERO, 0.001, 0);
		response = diskCache.write(theSameBlock);
		assertThat(response.getResult(), is(theSameBlock));

		Block block1 = new Block(1, ReplicaLevel.ZERO, 0.002, 0);
		response = diskCache.write(block1);
		assertThat(response.getResult(), is(block0));
		assertThat(response.getResult(), is(theSameBlock));

	}

	@Test
	public void readTest() {
		CacheParameter param = new CacheParameter(1.0, 3, 0.0001);
		diskCache = new DiskCache(0, param, BLOCK_SIZE);

		CacheResponse response;

		Block block0 = new Block(0, ReplicaLevel.ZERO, 0.001, 0);
		response = diskCache.read(block0);
		assertThat(response.getResponseTime(), is(0.0001));
		assertThat(response.getResult(), is(Block.NULL));

		response = diskCache.write(block0);
		assertThat(response.getResponseTime(), is(0.0001));
		assertThat(response.getResult(), is(block0));
		response = diskCache.read(block0);
		assertThat(response.getResponseTime(), is(0.0001));
		assertThat(response.getResult(), is(block0));
	}

}
