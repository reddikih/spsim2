package sim.storage.manager.buffer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class BufferMonitorTest {
	
	@Test
	public void getMeanArrivalRateOfWriteAccesses() {
		BufferMonitor monitor = new BufferMonitor();
		monitor.addWriteBlockCount(10);
		double lambda = monitor.getMeanArrivalRateOfWriteAccesses(1);
		assertThat(lambda, is(10.0));
		
		monitor.addWriteBlockCount(25);
		lambda = monitor.getMeanArrivalRateOfWriteAccesses(13.5);
		assertThat(lambda, is(2.0));
	}
	
	@Test
	public void meanArrivalRateWithZeroBuffer() {
		BufferMonitor monitor = new BufferMonitor();
		double lambda = monitor.getMeanArrivalRateOfWriteAccesses(1);
		assertThat(lambda, is(0.0));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void checkAssertionOnGettingMeanArrivalRate() {
		BufferMonitor monitor = new BufferMonitor();
		double lambda = monitor.getMeanArrivalRateOfWriteAccesses(10);
		assertThat(lambda, is(0.0));
		
		monitor.addWriteBlockCount(10);
		lambda = monitor.getMeanArrivalRateOfWriteAccesses(9);
		assertThat(lambda, is(0.0));
	}
	
	
}
