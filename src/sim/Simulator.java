package sim;

import java.util.Date;

import sim.statistics.RAPoSDAStats;
import sim.storage.cli.Client;
import sim.storage.manager.StorageManager;
import sim.storage.manager.StorageManagerFactory;

public class Simulator {

	private StorageManager getSM() {
		StorageManagerFactory smFactory =
			StorageManagerFactory.getStorageManagerFactory(
					Parameter.STORAGE_MANAGER_FACTORY);
		return smFactory.createStorageManager();
	}

	private Client getClient() {
		return new Client(Parameter.WORKLOAD_FILE_PATH);
	}

	private double run(Client client, StorageManager sm) {
		client.registerInitialData(sm);
		return client.run(sm);
	}

	private void close(double closeTime, StorageManager sm) {
		sm.close(closeTime);
		showStats(closeTime);
	}

	public void displayStartMessage() {
		System.out.println("Storage Simulator Version 2.");
		System.out.println("Starting time at: " + new Date().toString());
	}

	public void displayTerminateMessage(long elapsedTime) {
		System.out.println("End time at: " + new Date().toString());
		System.out.printf("Elapsed Time: %,d[s]\n", elapsedTime / 1000);
	}

	private void showStats(double closeTime) {
		RAPoSDAStats.showStatistics(closeTime);
	}

	public static void main(String[] args) {
		// TODO parse command line options

		long simStart = System.currentTimeMillis();

		Simulator sim = new Simulator();
		sim.displayStartMessage();

		Client client = sim.getClient();
		StorageManager sm = sim.getSM();

		double exeTime = sim.run(client, sm);

		sim.close(exeTime, sm);

		long simEnd = System.currentTimeMillis();
		sim.displayTerminateMessage(simEnd - simStart);
	}
}
