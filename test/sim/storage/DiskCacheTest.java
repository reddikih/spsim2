package sim.storage;

import java.math.BigInteger;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

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
		Block block0 = new Block(new BigInteger(String.valueOf(0)), ReplicaLevel.ZERO, 0.001);
		CacheResponse response = diskCache.write(block0);
		assertThat(response.getResponseTime(), is(0.0001));
		boolean arrayEquality = Arrays.equals(response.getReturnBlocks(), new Block[0]);
		assertThat(arrayEquality, is(true));

		Block block1 = new Block(new BigInteger(String.valueOf(1)), ReplicaLevel.ZERO, 0.002);
		response = diskCache.write(block1);
		arrayEquality = Arrays.equals(response.getReturnBlocks(), new Block[0]);
		assertThat(arrayEquality, is(true));

		Block block2 = new Block(new BigInteger(String.valueOf(2)), ReplicaLevel.ZERO, 0.003);
		response = diskCache.write(block2);
		arrayEquality = Arrays.equals(response.getReturnBlocks(), new Block[0]);
		assertThat(arrayEquality, is(true));

		Block block3 = new Block(new BigInteger(String.valueOf(3)), ReplicaLevel.ZERO, 0.004);
		response = diskCache.write(block3);
		arrayEquality = Arrays.equals(response.getReturnBlocks(), new Block[0]);
		assertThat(arrayEquality, is(true));

		// overflow check
		Block block4 = new Block(new BigInteger(String.valueOf(4)), ReplicaLevel.ZERO, 0.0019);
		response = diskCache.write(block4);
		arrayEquality = Arrays.equals(response.getReturnBlocks(), new Block[]{block0});
		assertThat(arrayEquality, is(true));

		// arrival time consistency check.
		Block block5 = new Block(new BigInteger(String.valueOf(5)), ReplicaLevel.ZERO, 0.005);
		response = diskCache.write(block5);
		arrayEquality = Arrays.equals(response.getReturnBlocks(), new Block[]{block4});
		assertThat(arrayEquality, is(true));
		assertThat(response.getResponseTime(), is(0.0001));
	}

	@Test
	public void writeTheSameBlock() {
		CacheParameter param = new CacheParameter(1.0, 3, 0.0001);
		diskCache = new DiskCache(0, param, BLOCK_SIZE);

		CacheResponse response;
		boolean arrayEquality;

		Block block0 = new Block(new BigInteger(String.valueOf(0)), ReplicaLevel.ZERO, 0.001);
		response = diskCache.write(block0);

		Block block1 = new Block(new BigInteger(String.valueOf(1)), ReplicaLevel.ZERO, 0.002);
		response = diskCache.write(block1);

		block0 = new Block(new BigInteger(String.valueOf(0)), ReplicaLevel.ZERO, 0.003);
		response = diskCache.write(block0);

		// block2 can be written on cache without replace.
		Block block2 = new Block(new BigInteger(String.valueOf(2)), ReplicaLevel.ZERO, 0.004);
		response = diskCache.write(block2);
		arrayEquality = Arrays.equals(response.getReturnBlocks(), new Block[0]);
		assertThat(arrayEquality, is(true));

		// In this time, block1 is replaced because of memory size limit.
		Block block3 = new Block(new BigInteger(String.valueOf(3)), ReplicaLevel.ZERO, 0.005);
		response = diskCache.write(block3);
		arrayEquality = Arrays.equals(response.getReturnBlocks(), new Block[]{block1});
		assertThat(arrayEquality, is(true));
		assertThat(response.getResponseTime(), is(0.0001));
	}

	@Test
	public void blockEquality() {
		CacheParameter param = new CacheParameter(1.0, 1, 0.0001);
		diskCache = new DiskCache(0, param, BLOCK_SIZE);

		CacheResponse response;
		boolean arrayEquality;

		Block block0 = new Block(new BigInteger(String.valueOf(0)), ReplicaLevel.ZERO, 0.000);
		response = diskCache.write(block0);
		arrayEquality = Arrays.equals(response.getReturnBlocks(), new Block[0]);
		assertThat(arrayEquality, is(true));

		Block theSameBlock = new Block(new BigInteger(String.valueOf(0)), ReplicaLevel.ZERO, 0.001);
		response = diskCache.write(theSameBlock);
		arrayEquality = Arrays.equals(response.getReturnBlocks(), new Block[0]);
		assertThat(arrayEquality, is(true));

		Block block1 = new Block(new BigInteger(String.valueOf(1)), ReplicaLevel.ZERO, 0.002);
		response = diskCache.write(block1);
		arrayEquality = Arrays.equals(response.getReturnBlocks(), new Block[]{block0});
		assertThat(arrayEquality, is(true));
		arrayEquality = Arrays.equals(response.getReturnBlocks(), new Block[]{theSameBlock});
		assertThat(arrayEquality, is(true));

	}

	@Test
	public void readTest() {
		CacheParameter param = new CacheParameter(1.0, 3, 0.0001);
		diskCache = new DiskCache(0, param, BLOCK_SIZE);

		CacheResponse response;
		boolean arrayEquality;

		Block block0 = new Block(new BigInteger(String.valueOf(0)), ReplicaLevel.ZERO, 0.001);
		response = diskCache.read(block0);
		assertThat(response.getResponseTime(), is(0.0001));
		arrayEquality = Arrays.equals(response.getReturnBlocks(), new Block[0]);
		assertThat(arrayEquality, is(true));

		response = diskCache.write(block0);
		assertThat(response.getResponseTime(), is(0.0001));
		response = diskCache.read(block0);
		assertThat(response.getResponseTime(), is(0.0001));
		arrayEquality = Arrays.equals(response.getReturnBlocks(), new Block[]{block0});
		assertThat(arrayEquality, is(true));
	}

}
