package sim.storage.manager.ddm;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import sim.Block;
import sim.Parameter;
import sim.storage.DiskResponse;
import sim.storage.HDDParameter;
import sim.storage.HardDiskDrive;
import sim.storage.state.DiskStateParameter;
import sim.storage.state.WithSleepDiskStateManager;
import sim.storage.util.DiskInfo;
import sim.storage.util.DiskState;
import sim.storage.util.ReplicaLevel;

@RunWith(JUnit4.class)
public class RAPoSDADataDiskManagerTest {

	private RAPoSDADataDiskManager ddm;
	private static HDDParameter dParam;
	private static DiskStateParameter dstParam;
	private static double aBlockResp;

	@BeforeClass
	public static void setUp() {
		dParam = new HDDParameter(
				100, //size
				Parameter.HDD_NUMBER_OF_PLATTER,
				Parameter.HDD_RPM,
				Parameter.HDD_CACHE_SIZE,
				Parameter.HDD_TRANSFER_RATE,
				Parameter.HDD_SECTORS_PER_TRACK,
				Parameter.HDD_FULL_STROKE_SEEK_TIME,
				Parameter.HDD_HEAD_SWITCH_OVERHEAD,
				Parameter.HDD_COMMAND_OVERHEAD
		);

		dstParam = new DiskStateParameter(
				Parameter.HDD_ACTIVE_POWER,
				Parameter.HDD_IDLE_POWER,
				Parameter.HDD_STANDBY_POWER,
				Parameter.HDD_SPINDOWN_ENERGY,
				Parameter.HDD_SPINUP_ENERGY,
				Parameter.HDD_SPINDOWN_TIME,
				Parameter.HDD_SPINUP_TIME);

		HardDiskDrive hdd = new HardDiskDrive(0, dParam);
		aBlockResp = hdd.write(
				new Block[]{new Block(0, 0.0, 0)});
	}

	private void init(int numDD, int numRep, double threshold) {
		HashMap<Integer, DataDisk> ddMap =
				new HashMap<Integer, DataDisk>();
		for (int i=0; i < numDD; i++) {
			WithSleepDiskStateManager stm = getSTM(threshold);
			DataDisk dd = getDataDisk(i, stm);
			ddMap.put(i, dd);
		}
		ddm = new RAPoSDADataDiskManager(numDD, numRep, ddMap);
	}

	private DataDisk getDataDisk(int id, WithSleepDiskStateManager stm) {
		return new DataDisk(id, dParam, stm);
	}

	private WithSleepDiskStateManager getSTM(double threshold) {
		return 	new WithSleepDiskStateManager(threshold, dstParam);
	}

	private void initiationWrite(Block[] blocks) {
		if (ddm == null)
			throw new IllegalStateException("ddm is null.");
		ddm.write(blocks);
	}

	private long d2l(double dVal) {
		// change time unit. seccond -> nano
		return (long)(dVal * 1000000000);
	}

	private void setOwnerDiskAndRepLevel(
			Block block, ReplicaLevel repLevel, int numdd) {
		block.setRepLevel(repLevel);
		block.setOwnerDiskId(
				(block.getPrimaryDiskId() +
				 block.getRepLevel().getValue()) % numdd);
	}

	@Test
	public void writeABlockToDDM() {
		int numdd = 4, numRep = 3;
		init(numdd, numRep, Double.MAX_VALUE);

		Block block0 = new Block(0, 0.0, 0);

		DiskResponse ddresp = ddm.write(new Block[]{block0});
		assertThat(ddresp.getResponseTime(), is(aBlockResp));
	}

	@Test
	public void writeSomeBlocks() {
		int numdd = 4, numRep = 3;
		init(numdd, numRep, Double.MAX_VALUE);

		DiskResponse ddresp;
		Block[] blocks;

		Block block00 = new Block(0, 0.0, 0);
		setOwnerDiskAndRepLevel(block00, ReplicaLevel.ZERO, numdd);
		Block block10 = new Block(1, 0.0, 1);
		setOwnerDiskAndRepLevel(block10, ReplicaLevel.ZERO, numdd);
		Block block20 = new Block(2, 0.0, 2);
		setOwnerDiskAndRepLevel(block20, ReplicaLevel.ZERO, numdd);
		Block block30 = new Block(3, 0.0, 3);
		setOwnerDiskAndRepLevel(block30, ReplicaLevel.ZERO, numdd);

		blocks = new Block[]{block00,block10,block20,block30};
		ddresp = ddm.write(blocks);
		assertThat(ddresp.getResponseTime(), is(aBlockResp));
		assertThat(Arrays.equals(ddresp.getResults(), blocks), is(true));

		Block block01 = new Block(4, 0.0, 0);
		setOwnerDiskAndRepLevel(block01, ReplicaLevel.ZERO, numdd);
		blocks = new Block[]{block01};
		ddresp = ddm.write(blocks);
		assertThat(ddresp.getResponseTime(), is(aBlockResp*2));
		assertThat(Arrays.equals(ddresp.getResults(), blocks), is(true));

		Block block11 = new Block(5, 1.0, 1);
		setOwnerDiskAndRepLevel(block11, ReplicaLevel.ZERO, numdd);
		Block block12 = new Block(6, 1.0, 1);
		setOwnerDiskAndRepLevel(block12, ReplicaLevel.ZERO, numdd);

		blocks = new Block[]{block11,block12};
		ddresp = ddm.write(blocks);
		assertThat(d2l(ddresp.getResponseTime()), is(d2l(aBlockResp*2)));
		assertThat(Arrays.equals(ddresp.getResults(), blocks), is(true));

		Block block13 = new Block(7, 1.0 + (aBlockResp / 2), 1);
		setOwnerDiskAndRepLevel(block13, ReplicaLevel.ZERO, numdd);
		blocks = new Block[]{block13};
		ddresp = ddm.write(blocks);
		assertThat(d2l(ddresp.getResponseTime()), is(d2l(aBlockResp * 2 + aBlockResp / 2)));
		assertThat(Arrays.equals(ddresp.getResults(), blocks), is(true));
	}

	@Test
	public void writeTheSameDiskWithDifferentReplicaLevelBlock() {
		int numdd = 4, numRep = 3;
		init(numdd, numRep, Double.MAX_VALUE);

		DiskResponse ddresp;
		Block[] blocks;

		Block block00R0 = new Block(0, 1.0, 0);
		setOwnerDiskAndRepLevel(block00R0, ReplicaLevel.ZERO, numdd);
		Block block20R2 = new Block(1, 1.0, 2);
		setOwnerDiskAndRepLevel(block20R2, ReplicaLevel.TWO, numdd);

		blocks = new Block[]{block00R0,block20R2};
		ddresp = ddm.read(blocks);
		assertThat(d2l(ddresp.getResponseTime()), is(d2l(aBlockResp * 2)));
		assertThat(Arrays.equals(ddresp.getResults(), blocks), is(true));
	}

	@Test
	public void readFromDDM() {
		int numdd = 4, numRep = 3;
		init(numdd, numRep, Double.MAX_VALUE);

		DiskResponse ddresp;
		Block[] blocks;

		Block block00 = new Block(0, 0.0, 0);
		setOwnerDiskAndRepLevel(block00, ReplicaLevel.ZERO, numdd);
		Block block10 = new Block(1, 0.0, 1);
		setOwnerDiskAndRepLevel(block10, ReplicaLevel.ZERO, numdd);
		Block block20 = new Block(2, 0.0, 2);
		setOwnerDiskAndRepLevel(block20, ReplicaLevel.ZERO, numdd);
		Block block30 = new Block(3, 0.0, 3);
		setOwnerDiskAndRepLevel(block30, ReplicaLevel.ZERO, numdd);

		blocks = new Block[]{block00,block10,block20,block30};
		initiationWrite(blocks);

		block20.setAccessTime(0.5);
		blocks = new Block[]{block20};
		ddresp = ddm.read(blocks);
		assertThat(d2l(ddresp.getResponseTime()), is(d2l(aBlockResp)));
		assertThat(Arrays.equals(ddresp.getResults(), blocks), is(true));

		// read from the same disk hold different replica level blocks.
		Block block31 = new Block(4, 1.0, 3);
		setOwnerDiskAndRepLevel(block31, ReplicaLevel.ZERO, numdd);
		Block block20R1 = new Block(2, 1.0, 2);
		setOwnerDiskAndRepLevel(block20R1, ReplicaLevel.ONE, numdd);

		blocks = new Block[]{block31,block20R1};
		ddresp = ddm.read(blocks);
		assertThat(d2l(ddresp.getResponseTime()), is(d2l(aBlockResp * 2)));
		assertThat(Arrays.equals(ddresp.getResults(), blocks), is(true));
	}

	@Test
	public void getRelatedDiskInfoTest() {
		int numdd = 4, numRep = 3;
		init(numdd, numRep, Double.MAX_VALUE);

		// key: expected diskId, value: expected ReplicaLevel.
		HashMap<Integer, ReplicaLevel> expected = new HashMap<Integer, ReplicaLevel>();
		List<DiskInfo> dInfos;

		Block block00 = new Block(0, 0.0, 0);
		setOwnerDiskAndRepLevel(block00, ReplicaLevel.ZERO, numdd);
		ddm.write(new Block[]{block00});
		Block block10 = new Block(1, 5.0, 1);
		setOwnerDiskAndRepLevel(block10, ReplicaLevel.TWO, numdd);
		ddm.write(new Block[]{block10});
		Block block20 = new Block(2, 10.0, 2);
		setOwnerDiskAndRepLevel(block20, ReplicaLevel.ONE, numdd);
		ddm.write(new Block[]{block20});
		Block block30 = new Block(3, 15.0, 3);
		setOwnerDiskAndRepLevel(block30, ReplicaLevel.ONE, numdd);
		ddm.write(new Block[]{block30});

		// related to disk=0,repLevel=0
		block00.setAccessTime(1.0);

		expected.put(0, ReplicaLevel.ZERO);
		expected.put(1, ReplicaLevel.ONE);
		expected.put(2, ReplicaLevel.TWO);
		dInfos = ddm.getRelatedDisksInfo(block00);
		assertThat(dInfos.size(), is(expected.size()));
		for (DiskInfo dInfo : dInfos) {
			assertThat(expected.containsKey(dInfo.getDiskId()), is(true));
			assertThat(expected.get(dInfo.getDiskId()), is(dInfo.getRepLevel()));
		}

		// related to disk=2,repLevel=1
		block20.setAccessTime(1.0);

		expected.clear();
		expected.put(2, ReplicaLevel.ZERO);
		expected.put(3, ReplicaLevel.ONE);
		expected.put(0, ReplicaLevel.TWO);
		dInfos = ddm.getRelatedDisksInfo(block20);
		assertThat(dInfos.size(), is(expected.size()));
		for (DiskInfo dInfo : dInfos) {
			assertThat(expected.containsKey(dInfo.getDiskId()), is(true));
			assertThat(expected.get(dInfo.getDiskId()), is(dInfo.getRepLevel()));
		}
	}

	@Test
	public void getLongestStandbyDiskInfoTest() {
		int numdd = 4, numRep = 3;
		init(numdd, numRep, 10.0);

		List<DiskInfo> dInfos;
		DiskInfo sleepest;

		Block block00 = new Block(0, 0.0, 0);
		setOwnerDiskAndRepLevel(block00, ReplicaLevel.ZERO, numdd);
		ddm.write(new Block[]{block00});
		Block block10 = new Block(1, 1.0, 1);
		setOwnerDiskAndRepLevel(block10, ReplicaLevel.ZERO, numdd);
		ddm.write(new Block[]{block10});
		Block block20 = new Block(2, 2.0, 2);
		setOwnerDiskAndRepLevel(block20, ReplicaLevel.ZERO, numdd);
		ddm.write(new Block[]{block20});
		Block block30 = new Block(3, 3.0, 3);
		setOwnerDiskAndRepLevel(block30, ReplicaLevel.ZERO, numdd);
		ddm.write(new Block[]{block30});

		// related to disk=0,repLevel=0
		// repLevel0 is disk0 last access =  0.0
		// repLevel1 is disk1 last access =  1.0
		// repLevel2 is disk2 last access = 2.0
		block00.setAccessTime(15.0);

		dInfos = ddm.getRelatedDisksInfo(block00);
		sleepest = ddm.getLongestStandbyDiskInfo(dInfos, block00.getAccessTime());
		assertThat(sleepest.getDiskId(), is(0));
		assertThat(sleepest.getDiskState(), is(DiskState.STANDBY));

		// following test should be consider disk spinup.
		// update last access time of disk 0.
		Block block40 = new Block(4, 4.0, 0);
		setOwnerDiskAndRepLevel(block40, ReplicaLevel.ZERO, numdd);
		ddm.write(new Block[]{block40});

		// related to disk=3,repLevel=1
		// repLevel0 is disk2 last access = 10.0
		// repLevel1 is disk3 last access = 15.0
		// repLevel2 is disk0 last access = 20.0
		Block block50 = new Block(5, 50.0, 2);
		setOwnerDiskAndRepLevel(block50, ReplicaLevel.ONE, numdd);

		dInfos = ddm.getRelatedDisksInfo(block50);
		sleepest = ddm.getLongestStandbyDiskInfo(dInfos, block50.getAccessTime());
		assertThat(sleepest.getDiskId(), is(2));
		assertThat(sleepest.getDiskState(), is(DiskState.STANDBY));
	}

}
