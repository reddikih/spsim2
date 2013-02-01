package sim.storage.manager.ddm;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.math.BigInteger;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import sim.Block;
import sim.Parameter;
import sim.storage.HDDParameter;
import sim.storage.HardDiskDrive;
import sim.storage.state.DataDiskStateManager;
import sim.storage.state.DiskStateParameter;
import sim.storage.state.IllegalDiskStateException;
import sim.storage.util.DiskState;

@RunWith(JUnit4.class)
public class DataDiskTest {

	private static double aBlockResp;
	private static HDDParameter diskParam;

	static {
		diskParam = new HDDParameter(
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
		HardDiskDrive hdd = new HardDiskDrive(0, diskParam);
		aBlockResp = hdd.write(
				new Block[]{new Block(new BigInteger("0"), 0.0, 0)});
	}

	private DataDiskStateManager getStm(double spdown_th) {
		DiskStateParameter stmParam =
			new DiskStateParameter(
					Parameter.HDD_ACTIVE_POWER,
					Parameter.HDD_IDLE_POWER,
					Parameter.HDD_STANDBY_POWER,
					Parameter.HDD_SPINDOWN_ENERGY,
					Parameter.HDD_SPINUP_ENERGY,
					Parameter.HDD_SPINDOWN_TIME,
					Parameter.HDD_SPINUP_TIME
			);
		return new DataDiskStateManager(spdown_th, stmParam);
	}

	@Test
	public void getStateTest() {
		Block[] blocks = null;
		DiskState currentState;
		double result;

		DataDisk dd = new DataDisk(0, diskParam, getStm(Parameter.SPINDOWN_THRESHOLD));

		Block b0 = new Block(new BigInteger("0"), 0.1, 0);
		blocks = new Block[]{b0};

		currentState = dd.getState(0.0);
		assertThat(currentState, is(DiskState.IDLE));

		result = dd.write(blocks);
		assertThat(result, is(aBlockResp));

		double lastIdleStartTime = b0.getAccessTime() + result;

		currentState = dd.getState(lastIdleStartTime + 5.0);
		assertThat(currentState, is(DiskState.IDLE));

		currentState = dd.getState(lastIdleStartTime + Parameter.SPINDOWN_THRESHOLD + 0.001);
		assertThat(currentState, is(DiskState.SPINDOWN));

		currentState = dd.getState(lastIdleStartTime + Parameter.SPINDOWN_THRESHOLD + 1);
		assertThat(currentState, is(DiskState.STANDBY));

		currentState = dd.getState(lastIdleStartTime + Parameter.SPINDOWN_THRESHOLD + 100);
		assertThat(currentState, is(DiskState.STANDBY));
	}

	@Test
	public void checkSpinningState() {
		DataDisk dd = new DataDisk(0, diskParam, getStm(Parameter.SPINDOWN_THRESHOLD));
		Block[] blocks = null;
		double result;

		Block b0 = new Block(new BigInteger("0"), 0.0, 0);
		blocks = new Block[]{b0};

		result = dd.write(blocks);
		assertThat(result, is(aBlockResp));
		assertThat(dd.isSpinning(result + Parameter.SPINDOWN_THRESHOLD), is(true));
		assertThat(dd.isSpinning(-1.0), is(true));
		assertThat(dd.isSpinning(result + Parameter.SPINDOWN_THRESHOLD - 0.001), is(true));
		assertThat(dd.isSpinning(result + Parameter.SPINDOWN_THRESHOLD + 0.001), is(false));
		assertThat(dd.isSpinning(result + Parameter.SPINDOWN_THRESHOLD + 10), is(false));
	}

	// TODO these test should use parametrized test.
	@Test
	public void spinUpTest1() {
		DataDisk dd = new DataDisk(0, diskParam, getStm(Parameter.SPINDOWN_THRESHOLD));
		Block[] blocks = null;
		double result;

		Block b0 = new Block(new BigInteger("0"), 0.0, 0);
		blocks = new Block[]{b0};

		result = dd.write(blocks);
		double accessTime = result + Parameter.SPINDOWN_THRESHOLD + Parameter.HDD_SPINDOWN_TIME + 0.001;
		assertThat(dd.isSpinning(accessTime), is(false));

		double delay = dd.spinUp(accessTime);
		assertThat(delay, is(Parameter.HDD_SPINUP_TIME));
	}

	@Test
	public void spinUpTest2() {
		DataDisk dd = new DataDisk(0, diskParam, getStm(Parameter.SPINDOWN_THRESHOLD));
		Block[] blocks = null;
		double result;

		Block b0 = new Block(new BigInteger("0"), 0.0, 0);
		blocks = new Block[]{b0};

		result = dd.write(blocks);
		double accessTime = result + Parameter.SPINDOWN_THRESHOLD + Parameter.HDD_SPINDOWN_TIME - (Parameter.HDD_SPINDOWN_TIME / 2);
		assertThat(dd.isSpinning(accessTime), is(false));

		double delay = dd.spinUp(accessTime);
		assertThat(delay, is((Parameter.HDD_SPINDOWN_TIME / 2) + Parameter.HDD_SPINUP_TIME));
	}

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void invalidSpinUpTest() {
		DataDisk dd = new DataDisk(0, diskParam, getStm(Parameter.SPINDOWN_THRESHOLD));
		Block[] blocks = null;
		double result;

		Block b0 = new Block(new BigInteger("0"), 0.0, 0);
		blocks = new Block[]{b0};

		result = dd.write(blocks);
		double accessTime = result + Parameter.SPINDOWN_THRESHOLD - 0.1;
		assertThat(dd.isSpinning(accessTime), is(true));

		thrown.expect(IllegalDiskStateException.class);
		dd.spinUp(accessTime);
	}

}
