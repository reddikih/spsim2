package sim.storage.cli;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ClientTest {
	
	@Test
	public void registerInitialDataCheck() {
		int blockSize = 1;
		int numReplica = 1;
		
		String filePath = "test/sim/storage/cli/workload";
		Client client = new Client(filePath);
		
		TestStorageManager sm =
				new TestStorageManager(null,null,null,blockSize,numReplica);
		
		client.registerInitialData(sm);
		assertThat(sm.requestMapSize(), is(10));
	}
}