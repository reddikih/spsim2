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
		System.out.println("=========================================");
		System.out.println("Storage Simulator Version 2.");
		System.out.println("Starting time at: " + new Date().toString());
		System.out.println("=========================================");
	}

	public void displayTerminateMessage(long elapsedTime) {
		System.out.println("End time at: " + new Date().toString());
		System.out.printf("Elapsed Time: %,d[s]\n", elapsedTime / 1000);
	}

	private void showStats(double closeTime) {
		RAPoSDAStats.showStatistics(closeTime);
	}

	private static void cliParse(String... commands) {
		String comVal = null;
		for (String command : commands) {
			if (command.startsWith("-DD=")) {
				comVal = command.substring(4);
				Parameter.NUMBER_OF_DISKS_PER_CACHE_GROUP = Integer.parseInt(comVal);
				System.out.println("DataDisks=" + Parameter.NUMBER_OF_DISKS_PER_CACHE_GROUP);
			} else if (command.startsWith("-CD=")) {
				comVal = command.substring(4);
				Parameter.NUMBER_OF_CACHE_DISKS = Integer.parseInt(comVal);
				System.out.println("CacheDisks=" + Parameter.NUMBER_OF_CACHE_DISKS);
			} else if (command.startsWith("-R=")) {
				comVal = command.substring(3);
				Parameter.NUMBER_OF_REPLICA = Integer.parseInt(comVal);
				System.out.println("Replicas=" + Parameter.NUMBER_OF_REPLICA);
			} else if (command.startsWith("-SM=")) {
				comVal = command.substring(4);
				Parameter.STORAGE_MANAGER_FACTORY = comVal;
				System.out.println("StorageManagerFactory=" + Parameter.STORAGE_MANAGER_FACTORY);
			} else if (command.startsWith("-CMA=")) {
				comVal = command.substring(5);
				Parameter.CACHE_MEMORY_ASSIGNOR = comVal;
				System.out.println("CacheMemoryAssignor=" + Parameter.CACHE_MEMORY_ASSIGNOR);
			} else if (command.startsWith("-CMF=")) {
				comVal = command.substring(5);
				Parameter.CACHE_MEMORY_FACTORY = comVal;
				System.out.println("CacheMemoryFactory=" + Parameter.CACHE_MEMORY_FACTORY);
			} else if (command.startsWith("-W=")) {
				comVal = command.substring(3);
				Parameter.WORKLOAD_FILE_PATH = comVal;
				System.out.println("Workload=" + Parameter.WORKLOAD_FILE_PATH);
			} else if (command.startsWith("-NM=")) {
				comVal = command.substring(4);
				Parameter.NUMBER_OF_CACHE_MEMORIES = Integer.parseInt(comVal);
				System.out.println("NumberOfCacheMemories=" + Parameter.NUMBER_OF_CACHE_MEMORIES);
			} else if (command.startsWith("-MS=")) {
				comVal = command.substring(4);
				Parameter.CACHE_MEMORY_SIZE = Long.parseLong(comVal) * 1024 * 1024 * 1024;
				System.out.println("MemorySize=" + Parameter.CACHE_MEMORY_SIZE);
			}
		}
	}

	public static void main(String[] args) {
		long simStart = System.currentTimeMillis();

		Simulator sim = new Simulator();
		sim.displayStartMessage();

		// TODO parse command line options
		cliParse(args);

		Client client = sim.getClient();
		StorageManager sm = sim.getSM();

		double exeTime = sim.run(client, sm);

		sim.close(exeTime, sm);

		long simEnd = System.currentTimeMillis();
		sim.displayTerminateMessage(simEnd - simStart);
	}
}
