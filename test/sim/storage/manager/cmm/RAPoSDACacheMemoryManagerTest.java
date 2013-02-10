package sim.storage.manager.cmm;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import sim.Block;
import sim.Parameter;
import sim.storage.CacheParameter;
import sim.storage.CacheResponse;
import sim.storage.manager.cmm.assignor.CacheStripingAssignor;
import sim.storage.manager.cmm.assignor.IAssignor;
import sim.storage.util.DiskInfo;
import sim.storage.util.DiskState;
import sim.storage.util.ReplicaLevel;

@RunWith(JUnit4.class)
public class RAPoSDACacheMemoryManagerTest {

	private RAPoSDACacheMemoryManager cmm;
	private IAssignor assignor;
	private CacheParameter cmParam;

	private void init(int numReplica, int numCacheMemory, int cacheSize, int blockSize) {
		this.assignor = new CacheStripingAssignor(numCacheMemory);
		this.cmParam = new CacheParameter(1.0, cacheSize, Parameter.CACHE_MEMORY_LATENCY);
		HashMap<Integer, CacheMemory> cacheMemories = new HashMap<Integer, CacheMemory>();
		for (int i=0; i < numCacheMemory; i++) {
			CacheMemory cm = new CacheMemory(i, numReplica, cmParam, blockSize);
			cacheMemories.put(i, cm);
		}
		cmm = new RAPoSDACacheMemoryManager(cacheMemories, assignor, numReplica);
	}

	private Block[] generateBlocks(
			int primDiskId,
			int originId,
			double originTime,
			int numReplica,
			int numdd) {
		Block[] result = new Block[numReplica];
		for (ReplicaLevel rLevel : ReplicaLevel.values()) {
			numReplica--;
			if (numReplica < 0) break;
			Block block = new Block(new BigInteger(String.valueOf(originId++)), originTime++, primDiskId);
			block.setOwnerDiskId((primDiskId + rLevel.getValue()) % numdd);
			block.setRepLevel(rLevel);
			result[rLevel.getValue()] = block;
		}
		return result;
	}

	@Test
	public void writeABlockWithoutOverflow() {
		int numReplica = 2, numCacheMemory = 2, cacheSize = 2, blockSize = 1;
		init(numReplica, numCacheMemory, cacheSize, blockSize);

		RAPoSDACacheWriteResponse response;

		Block block0 = new Block(new BigInteger("0"), 0.0, 0);
		block0.setOwnerDiskId(0);
		block0.setRepLevel(ReplicaLevel.ZERO);
		response = cmm.write(block0);
		assertThat(response.getResponseTime(), is(Parameter.CACHE_MEMORY_LATENCY));
		assertThat(response.getMaxBufferDiskId(), is(0));
		assertThat(response.getOverflows().length, is(0));
	}

	@Test
	public void writeBlocksWithOverflow() {
		int numReplica = 2, numCacheMemory = 2, cacheSize = 4, blockSize = 1;
		init(numReplica, numCacheMemory, cacheSize, blockSize);

		RAPoSDACacheWriteResponse response;

		Block blockP0 = new Block(new BigInteger("0"), 0.0, 0);
		blockP0.setOwnerDiskId(0);
		blockP0.setRepLevel(ReplicaLevel.ZERO);
		Block blockR0 = new Block(new BigInteger("1"), 0.0, 0);
		blockR0.setOwnerDiskId(1);
		blockR0.setRepLevel(ReplicaLevel.ONE);
		Block blockP1 = new Block(new BigInteger("2"), 0.0, 1);
		blockP1.setOwnerDiskId(1);
		blockP1.setRepLevel(ReplicaLevel.ZERO);
		Block blockR1 = new Block(new BigInteger("3"), 0.0, 1);
		blockR1.setOwnerDiskId(2);
		blockR1.setRepLevel(ReplicaLevel.ONE);
		Block blockP2 = new Block(new BigInteger("4"), 0.0, 2);
		blockP2.setOwnerDiskId(2);
		blockP2.setRepLevel(ReplicaLevel.ZERO);
		Block blockR2 = new Block(new BigInteger("5"), 0.0, 2);
		blockR2.setOwnerDiskId(3);
		blockR2.setRepLevel(ReplicaLevel.ONE);
		Block blockP3 = new Block(new BigInteger("6"), 0.0, 3);
		blockP3.setOwnerDiskId(3);
		blockP3.setRepLevel(ReplicaLevel.ZERO);
		Block blockR3 = new Block(new BigInteger("7"), 0.0, 3);
		blockR3.setOwnerDiskId(4);
		blockR3.setRepLevel(ReplicaLevel.ONE);

		response = cmm.write(blockP0);
		assertThat(response.getResponseTime(), is(Parameter.CACHE_MEMORY_LATENCY));
		assertThat(response.getMaxBufferDiskId(), is(0));
		assertThat(response.getOverflows().length, is(0));
		response = cmm.write(blockR0);
		assertThat(response.getResponseTime(), is(Parameter.CACHE_MEMORY_LATENCY));
		assertThat(response.getMaxBufferDiskId(), is(1));
		assertThat(response.getOverflows().length, is(0));
		response = cmm.write(blockP1);
		assertThat(response.getResponseTime(), is(Parameter.CACHE_MEMORY_LATENCY));
		assertThat(response.getMaxBufferDiskId(), is(1));
		assertThat(response.getOverflows().length, is(0));
		response = cmm.write(blockR1);
		assertThat(response.getResponseTime(), is(Parameter.CACHE_MEMORY_LATENCY));
		assertThat(response.getMaxBufferDiskId(), is(2));
		assertThat(response.getOverflows().length, is(0));
		response = cmm.write(blockP2);
		assertThat(response.getResponseTime(), is(Parameter.CACHE_MEMORY_LATENCY));
		assertThat(response.getMaxBufferDiskId(), is(0));
		assertThat(response.getOverflows().length, is(0));
		response = cmm.write(blockR2);
		assertThat(response.getResponseTime(), is(Parameter.CACHE_MEMORY_LATENCY));
		assertThat(response.getMaxBufferDiskId(), is(1));
		assertThat(response.getOverflows().length, is(0));
		response = cmm.write(blockP3);
		assertThat(response.getResponseTime(), is(Parameter.CACHE_MEMORY_LATENCY));
		assertThat(response.getMaxBufferDiskId(), is(1));
		assertThat(response.getOverflows().length, is(0));
		response = cmm.write(blockR3);
		assertThat(response.getResponseTime(), is(Parameter.CACHE_MEMORY_LATENCY));
		assertThat(response.getMaxBufferDiskId(), is(2));
		assertThat(response.getOverflows().length, is(0));

		// idenpotent check
		response = cmm.write(blockP3);
		assertThat(response.getResponseTime(), is(Parameter.CACHE_MEMORY_LATENCY));
		assertThat(response.getMaxBufferDiskId(), is(1));
		assertThat(response.getOverflows().length, is(0));

		// overflow check. CM0 should overflow.
		Block blockP4 = new Block(new BigInteger("8"), 0.0, 2);
		blockP4.setOwnerDiskId(2);
		blockP4.setRepLevel(ReplicaLevel.ZERO);
		response = cmm.write(blockP4);
		assertThat(response.getResponseTime(), is(Parameter.CACHE_MEMORY_LATENCY));
		assertThat(response.getMaxBufferDiskId(), is(2));
		assertThat(response.getOverflows().length, is(3));

		// overflow check. replica 1 of CM1 should overflow.
		Block blockR4 = new Block(new BigInteger("8"), 0.0, 2);
		blockR4.setOwnerDiskId(3);
		blockR4.setRepLevel(ReplicaLevel.ONE);
		response = cmm.write(blockR4);
		assertThat(response.getResponseTime(), is(Parameter.CACHE_MEMORY_LATENCY));
		assertThat(response.getMaxBufferDiskId(), is(3));
		assertThat(response.getOverflows().length, is(3));
	}

	@Test
	public void removeBufferedBlocksAfterOverflow() {
		int numReplica = 2, numCacheMemory = 2, cacheSize = 6, blockSize = 1;
		init(numReplica, numCacheMemory, cacheSize, blockSize);

		RAPoSDACacheWriteResponse response;
		Block[] blocks;

		blocks = generateBlocks(0, 0, 0.0, 2, 10);
		for (Block b : blocks) {
			response = cmm.write(b);
			assertThat(response.getResponseTime(), is(Parameter.CACHE_MEMORY_LATENCY));
			assertThat(response.getOverflows().length, is(0));
		}
		blocks = generateBlocks(0, 2, 2.0, 2, 10);
		for (Block b : blocks) {
			response = cmm.write(b);
			assertThat(response.getResponseTime(), is(Parameter.CACHE_MEMORY_LATENCY));
			assertThat(response.getOverflows().length, is(0));
		}
		blocks = generateBlocks(0, 4, 4.0, 2, 10);
		for (Block b : blocks) {
			response = cmm.write(b);
			assertThat(response.getResponseTime(), is(Parameter.CACHE_MEMORY_LATENCY));
			assertThat(response.getOverflows().length, is(0));
		}

		// overflow check. replica 1 of CM1 should overflow.
		Block overflow = new Block(new BigInteger("8"), 0.0, 0);
		overflow.setOwnerDiskId(0);
		overflow.setRepLevel(ReplicaLevel.ZERO);
		response = cmm.write(overflow);
		assertThat(response.getResponseTime(), is(Parameter.CACHE_MEMORY_LATENCY));
		assertThat(response.getMaxBufferDiskId(), is(0));
		assertThat(response.getOverflows().length, is(4));

		// remove buffer data at overflow region
		Block[] toRemove = response.getOverflows();
		for (Block b : toRemove) {
			CacheResponse resp = cmm.remove(b);
			assertThat(resp.getResult(), is(b));
		}
		Block notOverflow = new Block(new BigInteger("9"), 0.0, 2);
		notOverflow.setOwnerDiskId(2);
		notOverflow.setRepLevel(ReplicaLevel.ZERO);
		response = cmm.write(notOverflow);
		assertThat(response.getResponseTime(), is(Parameter.CACHE_MEMORY_LATENCY));
		assertThat(response.getMaxBufferDiskId(), is(2));
		assertThat(response.getOverflows().length, is(0));
	}

	@Test
	public void readFromCMM() {
		int numReplica = 2, numCacheMemory = 2, cacheSize = 4, blockSize = 1;
		init(numReplica, numCacheMemory, cacheSize, blockSize);

		RAPoSDACacheWriteResponse response;
		Block[] blocks;

		blocks = generateBlocks(0, 0, 0.0, 2, 10);
		for (Block b : blocks) {
			response = cmm.write(b);
			assertThat(response.getResponseTime(), is(Parameter.CACHE_MEMORY_LATENCY));
			assertThat(response.getOverflows().length, is(0));
		}
		blocks = generateBlocks(0, 2, 2.0, 2, 10);
		for (Block b : blocks) {
			response = cmm.write(b);
			assertThat(response.getResponseTime(), is(Parameter.CACHE_MEMORY_LATENCY));
			assertThat(response.getOverflows().length, is(0));
		}
		Block read = new Block(new BigInteger("0"), 5.0, 0);
		read.setOwnerDiskId(0);
		read.setRepLevel(ReplicaLevel.ZERO);
		CacheResponse resp = cmm.read(read);
		assertThat(resp.getResponseTime(), is(Parameter.CACHE_MEMORY_LATENCY));
		assertThat(resp.getResult(), is(read));

		Block notExist = new Block(new BigInteger("11"), 6.0, 0);
		notExist.setOwnerDiskId(0);
		notExist.setRepLevel(ReplicaLevel.ZERO);
		resp = cmm.read(notExist);
		assertThat(resp.getResponseTime(), is(Parameter.CACHE_MEMORY_LATENCY));
		assertThat(resp.getResult(), is(Block.NULL));

//		Block untilExist = new Block(new BigInteger("2"), 1.5, 0);
//		untilExist.setOwnerDiskId(0);
//		untilExist.setRepLevel(ReplicaLevel.ZERO);
//		resp = cmm.read(untilExist);
//		assertThat(resp.getResponseTime(), is(Parameter.CACHE_MEMORY_LATENCY));
//		assertThat(resp.getResult(), is(Block.NULL));
	}

	@Test
	public void getMaxBufferDiskWithReplicaThree() {
		int numdd = 6, primaryDiskId = 5;
		int numReplica = 3, numCacheMemory = 3, cacheSize = 12, blockSize = 1;
		init(numReplica, numCacheMemory, cacheSize, blockSize);

		Block[] blocks;

		blocks = generateBlocks(primaryDiskId, 0, 0.0, numReplica, numdd);
		for (Block b : blocks) {
			cmm.write(b);
		}

		List<DiskInfo> relDisks = new ArrayList<DiskInfo>();
		for (ReplicaLevel repLevel : ReplicaLevel.values()) {
			if (repLevel.getValue() >= numReplica) break;
			relDisks.add(new DiskInfo(
					(primaryDiskId + repLevel.getValue()) % numdd,
					DiskState.ACTIVE,
					repLevel)
			);
		}

		// now CM0.R=1, CM1.R=2, CM2.R=0 are has one block each other.
		// And then, write into CM0.R=1, cause CM0 has max buffer.
		// Therefore, disk0 is determinded as the max buffer disk.
		Block block = new Block(new BigInteger("3"), 4.0, primaryDiskId);
		block.setRepLevel(ReplicaLevel.ONE);
		cmm.write(block);
		DiskInfo maxBuffDisk = cmm.getMaxBufferDisk(relDisks);
		assertThat(maxBuffDisk.getDiskId(), is(0));

		// One more check.
		// Now that case adding two block into CM1.R2 region.
		// Thus, disk1 is determined as the max buffer disk.
		block = new Block(new BigInteger("4"), 5.0, primaryDiskId);
		block.setRepLevel(ReplicaLevel.TWO);
		cmm.write(block);
		block = new Block(new BigInteger("5"), 5.0, primaryDiskId);
		block.setRepLevel(ReplicaLevel.TWO);
		cmm.write(block);
		maxBuffDisk = cmm.getMaxBufferDisk(relDisks);
		assertThat(maxBuffDisk.getDiskId(), is(1));

	}

	@Test
	public void getMaxBufferDiskWithReplicaFour() {
		int numdd = 8, primaryDiskId = 6;
		int numReplica = 4, numCacheMemory = 4, cacheSize = 8, blockSize = 1;
		init(numReplica, numCacheMemory, cacheSize, blockSize);

		Block[] blocks;

		blocks = generateBlocks(primaryDiskId, 0, 0.0, numReplica, numdd);
		for (Block b : blocks) {
			cmm.write(b);
		}

		List<DiskInfo> relDisks = new ArrayList<DiskInfo>();
		for (ReplicaLevel repLevel : ReplicaLevel.values()) {
			if (repLevel.getValue() >= numReplica) break;
			relDisks.add(new DiskInfo(
					(primaryDiskId + repLevel.getValue()) % numdd,
					DiskState.ACTIVE,
					repLevel)
			);
		}

		// now CM0.R=2, CM1.R=3, CM2.R=0, CM3.R=1 are has one block each other.
		// And then, write into D4.R2(into CM0.R=2), cause CM0 has max buffer.
		// Therefore, disk0 is determinded as the max buffer disk.
		Block block = new Block(new BigInteger("4"), 5.0, 2);
		block.setRepLevel(ReplicaLevel.TWO);
		cmm.write(block);
		DiskInfo maxBuffDisk = cmm.getMaxBufferDisk(relDisks);
		assertThat(maxBuffDisk.getDiskId(), is(0));
	}

	@Test
	public void serchFromAllRelatedCacheMemories() {
		int primaryDiskId = 0, numdd = 10;
		int numReplica = 3, numCacheMemory = 3, cacheSize = 6, blockSize = 1;
		init(numReplica, numCacheMemory, cacheSize, blockSize);

		Block[] blocks;

		Block blockR0 = new Block(new BigInteger("0"), 0.0, primaryDiskId);
		blockR0.setRepLevel(ReplicaLevel.ZERO);
		Block blockR1 = new Block(new BigInteger("0"), 0.0, primaryDiskId);
		blockR1.setRepLevel(ReplicaLevel.ONE);
		Block blockR2 = new Block(new BigInteger("0"), 0.0, primaryDiskId);
		blockR2.setRepLevel(ReplicaLevel.TWO);
		blocks = new Block[]{blockR0, blockR1, blockR2};
		for (Block b : blocks) {
			cmm.write(b);
		}

		// At this time, the block replicas with id 0 are cached CM0 to CM2
		CacheResponse cResp = cmm.read(blockR0);
		assertThat(cResp.getResponseTime(), is(Parameter.CACHE_MEMORY_LATENCY));
		assertThat(cResp.getResult(), is(blockR0));

		// if remove blockR0(primary), since replicas are still living in cacheds
		// (r=1, r=2), read the blockR0 will hit.
		cmm.remove(blockR0);
		cResp = cmm.read(blockR0);
		assertThat(cResp.getResponseTime(), is(Parameter.CACHE_MEMORY_LATENCY));
		assertThat(cResp.getResult(), is(blockR0));

		// if remove blockR1(r=1), since replicas are still living in cacheds
		// (r=2), read the blockR0 will hit.
		cmm.remove(blockR1);
		cResp = cmm.read(blockR0);
		assertThat(cResp.getResponseTime(), is(Parameter.CACHE_MEMORY_LATENCY));
		assertThat(cResp.getResult(), is(blockR0));

		// if remove blockR1(r=2), since there are no replicas in the cacheds
		// , read the blockR0 will not hit.
		cmm.remove(blockR2);
		cResp = cmm.read(blockR0);
		assertThat(cResp.getResponseTime(), is(Parameter.CACHE_MEMORY_LATENCY));
		assertThat(cResp.getResult(), is(Block.NULL));

	}
}
