package sim.storage.cli;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sim.Request;
import sim.Response;
import sim.statistics.RAPoSDAStats;
import sim.storage.manager.RAPoSDAStorageManager;

public class Client {

	private static final int READ_BUFFER = 1024;
	private WorkloadReader wlReader;
	private static Logger logger = LoggerFactory.getLogger(Client.class);

	public Client(String workloadPath) {
		this.wlReader = new WorkloadReader(workloadPath);
	}

	public void registerInitialData(RAPoSDAStorageManager sm) {
		InitialDataInfo initInfo = this.wlReader.getInitialDataInfo();

		int baseSize = initInfo.getLowerBound();
		int sizeDiff = initInfo.getUpperBound() - baseSize;
		if (sizeDiff == 0) sizeDiff = 1;
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
				logger.trace(
						String.format(
								"id:%d arrival:%.5f response:%.5f RTT:%.5f",
								response.getKey(),
								request.getArrvalTime(),
								tempTime,
								response.getResponseTime()));
			}
		}
		return lastResponse;
	}
}
