package sim.storage.cli;

import java.util.Random;

import sim.Request;
import sim.Response;
import sim.statistics.RAPoSDAStats;
import sim.storage.manager.RAPoSDAStorageManager;

public class Client {

	private static final int READ_BUFFER = 1024;
	private WorkloadReader wlReader;
	
	public Client(String workloadPath) {
		this.wlReader = new WorkloadReader(workloadPath);
	}
	
	public void registerInitialData(RAPoSDAStorageManager sm) {
		InitialDataInfo initInfo = this.wlReader.getInitialDataInfo();
		
		int baseSize = initInfo.getLowerBound();
		int sizeDiff = initInfo.getUpperBound() - baseSize;
		int numOfFiles = initInfo.getNumberOfFiles();
		Random random = new Random();
		
		for (int i=0; i<numOfFiles; i++) {
			int offset = random.nextInt(sizeDiff);
			sm.register(i, baseSize + offset);			
		}
	}

	public double run(RAPoSDAStorageManager sm) {
		double lastResponse = Double.MIN_VALUE;
		while (true) {
			Request[] requests = wlReader.getRequests(READ_BUFFER);
			assert requests != null;
			if (requests.length == 0) break;
			for (Request request : requests) {
				Response response = sm.write(request);
				double tempTime = request.getArrvalTime() + response.getResponseTime();
				lastResponse = lastResponse < tempTime ? tempTime : lastResponse;
				
				RAPoSDAStats.addResponseTime(response.getResponseTime());
			}
		}
		return lastResponse;
	}
}
