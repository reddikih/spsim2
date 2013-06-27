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
			} else if (command.startsWith("-CD=")) {
				comVal = command.substring(4);
				Parameter.NUMBER_OF_CACHE_DISKS = Integer.parseInt(comVal);
			} else if (command.startsWith("-R=")) {
				comVal = command.substring(3);
				Parameter.NUMBER_OF_REPLICA = Integer.parseInt(comVal);
			} else if (command.startsWith("-SM=")) {
				comVal = command.substring(4);
				Parameter.STORAGE_MANAGER_FACTORY = comVal;
			} else if (command.startsWith("-CMA=")) {
				comVal = command.substring(5);
				Parameter.CACHE_MEMORY_ASSIGNOR = comVal;
			} else if (command.startsWith("-CMF=")) {
				comVal = command.substring(5);
				Parameter.CACHE_MEMORY_FACTORY = comVal;
			} else if (command.startsWith("-W=")) {
				comVal = command.substring(3);
				Parameter.WORKLOAD_FILE_PATH = comVal;
			} else if (command.startsWith("-NM=")) {
				comVal = command.substring(4);
				Parameter.NUMBER_OF_CACHE_MEMORIES = Integer.parseInt(comVal);
			} else if (command.startsWith("-MS=")) {
				comVal = command.substring(4);
				Parameter.CACHE_MEMORY_SIZE = Long.parseLong(comVal) * 1024 * 1024 * 1024;
			} else if (command.startsWith("-BM=")) {
				comVal = command.substring(4);
				Parameter.BUFFER_MANAGER_FACTORY = comVal;
			} else if (command.startsWith("-DEBUG=")) {
				comVal = command.substring(4);
				Parameter.DEBUG_FLAG = Boolean.parseBoolean(comVal);
			}
		}
		if (Parameter.DEBUG_FLAG) {
			System.out.println(String.format("DataDisks             = %d (%ddisk/mem)", Parameter.NUMBER_OF_CACHE_MEMORIES * Parameter.NUMBER_OF_DISKS_PER_CACHE_GROUP, Parameter.NUMBER_OF_DISKS_PER_CACHE_GROUP));
			System.out.println(String.format("CacheDisks            = %d", Parameter.NUMBER_OF_CACHE_DISKS));
			System.out.println(String.format("Replicas              = %d", Parameter.NUMBER_OF_REPLICA));
			System.out.println(String.format("NumberOfCacheMemories = %d", Parameter.NUMBER_OF_CACHE_MEMORIES));
			System.out.println(String.format("MemorySize(1 unit)    = %,dByte", Parameter.CACHE_MEMORY_SIZE));
			System.out.println(String.format("CacheMemoryAssignor   = %s", Parameter.CACHE_MEMORY_ASSIGNOR));
			System.out.println(String.format("CacheMemoryFactory    = %s", Parameter.CACHE_MEMORY_FACTORY));
			System.out.println(String.format("StorageManagerFactory = %s", Parameter.STORAGE_MANAGER_FACTORY));
			System.out.println(String.format("BufferManagerFactory  = %s", Parameter.BUFFER_MANAGER_FACTORY));
			System.out.println(String.format("Workload              = %s", Parameter.WORKLOAD_FILE_PATH));
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
