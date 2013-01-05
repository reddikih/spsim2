package sim.storage.manager.cmm;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.math.BigInteger;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import sim.Block;
import sim.storage.util.ReplicaLevel;

@RunWith(JUnit4.class)
public class RegionTest {

	@Test
	public void writeTest() {
		Region region = new Region(2);
		Block result = null;

		Block block0 = new Block(new BigInteger("0"), ReplicaLevel.ZERO, 0.0, 0);
		Block block1 = new Block(new BigInteger("1"), ReplicaLevel.ZERO, 0.1, 0);
		Block block2 = new Block(new BigInteger("2"), ReplicaLevel.ZERO, 0.1, 0);

		result = region.write(block0);
		assertThat(result, is(block0));
		result = region.write(block1);
		assertThat(result, is(block1));
		result = region.write(block2);
		assertThat(result, is(Block.NULL));

		block0.setAccessTime(2.0);
		result = region.write(block0);
		assertThat(result, is(block0));
	}

	@Test
	public void maxBufferDiskTest() {
		Region region = new Region(10);
		Block result = null;

		Block block0 = new Block(new BigInteger("0"), ReplicaLevel.ZERO, 0.0, 0);
		Block block1 = new Block(new BigInteger("1"), ReplicaLevel.ZERO, 0.1, 0);
		Block block2 = new Block(new BigInteger("2"), ReplicaLevel.ZERO, 0.1, 1);
		Block block3 = new Block(new BigInteger("3"), ReplicaLevel.ZERO, 0.1, 1);
		Block block4 = new Block(new BigInteger("4"), ReplicaLevel.ZERO, 0.1, 1);
		Block block5 = new Block(new BigInteger("5"), ReplicaLevel.ZERO, 0.1, 2);
		Block block6 = new Block(new BigInteger("6"), ReplicaLevel.ZERO, 0.1, 2);
		Block block7 = new Block(new BigInteger("7"), ReplicaLevel.ZERO, 0.1, 2);
		Block block8 = new Block(new BigInteger("8"), ReplicaLevel.ZERO, 0.1, 2);
		Block block9 = new Block(new BigInteger("9"), ReplicaLevel.ZERO, 0.1, 3);

		result = region.write(block0);
		result = region.write(block1);
		result = region.write(block2);
		result = region.write(block3);
		result = region.write(block4);
		result = region.write(block5);
		result = region.write(block6);
		result = region.write(block7);
		result = region.write(block8);
		result = region.write(block9);
		assertThat(region.getMaxBufferLenghtDiskId(), is(2));

		region.remove(block8);
		assertThat(region.getMaxBufferLenghtDiskId(), is(1));
		result = region.remove(block7);
		assertThat(region.getMaxBufferLenghtDiskId(), is(1));
		assertThat(result, is(block7));
	}

	@Test
	public void readTest() {
		Region region = new Region(3);
		Block result = null;

		Block block0 = new Block(new BigInteger("0"), ReplicaLevel.ZERO, 0.0, 0);
		Block block1 = new Block(new BigInteger("1"), ReplicaLevel.ZERO, 0.1, 0);
		Block block2 = new Block(new BigInteger("2"), ReplicaLevel.ZERO, 0.1, 1);
		Block block3 = new Block(new BigInteger("3"), ReplicaLevel.ZERO, 0.1, 1);

		result = region.write(block0);
		result = region.write(block1);
		result = region.write(block2);
		result = region.read(block0);
		assertThat(result, is(block0));
		assertThat(result, not(block1));

		result = region.read(block3);
		assertThat(result, is(Block.NULL));
	}

	@Test
	public void getBlocks() {
		Region region = new Region(3);
		Block result = null;

		Block block0 = new Block(new BigInteger("0"), ReplicaLevel.ZERO, 0.0, 0);
		Block block1 = new Block(new BigInteger("1"), ReplicaLevel.ZERO, 0.1, 1);
		Block block2 = new Block(new BigInteger("2"), ReplicaLevel.ZERO, 0.2, 2);

		result = region.write(block0);
		result = region.write(block1);
		result = region.write(block2);

		Block[] blocks = region.getBlocks();
		for (int i = 0; i < blocks.length; i++) {
			if (blocks[i].equals(block2)) {
				blocks[i] = null;
			}
		}
		result = region.read(block2);
		assertThat(result, is(block2));
	}

}
