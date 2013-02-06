package sim;

import java.util.Date;
import java.util.HashMap;

import sim.statistics.RAPoSDAStats;
import sim.storage.CacheParameter;
import sim.storage.HDDParameter;
import sim.storage.cli.Client;
import sim.storage.manager.RAPoSDAStorageManager;
import sim.storage.manager.cdm.CacheDisk;
import sim.storage.manager.cdm.RAPoSDACacheDiskManager;
import sim.storage.manager.cmm.CacheMemory;
import sim.storage.manager.cmm.RAPoSDACacheMemoryManager;
import sim.storage.manager.cmm.assignor.BalancedAssignor;
import sim.storage.manager.cmm.assignor.IAssignor;
import sim.storage.manager.ddm.DataDisk;
import sim.storage.manager.ddm.RAPoSDADataDiskManager;
import sim.storage.state.CacheDiskStateManager;
import sim.storage.state.DataDiskStateManager;
import sim.storage.state.DiskStateParameter;

public class Simulator {

	private RAPoSDAStorageManager getSM() {
		int blockSize = Parameter.BLOCK_SIZE;
		int numRep = Parameter.NUMBER_OF_REPLICA;

		RAPoSDACacheMemoryManager cmm = getCMM();
		RAPoSDACacheDiskManager cdm = getCDM();
		RAPoSDADataDiskManager ddm = getDDM();

		return new RAPoSDAStorageManager(cmm, cdm, ddm, blockSize, numRep);
	}

	private RAPoSDACacheMemoryManager getCMM() {
		int numcm = Parameter.NUMBER_OF_CACHE_MEMORIES;
		HashMap<Integer, CacheMemory> cacheMemories =
			new HashMap<Integer, CacheMemory>();

		CacheParameter param = new CacheParameter(
				Parameter.CACHE_MEMORY_THRESHOLD,
				Parameter.CACHE_MEMORY_SIZE,
				Parameter.CACHE_MEMORY_LATENCY
		);

		for (int i=0; i < numcm; i++) {
			CacheMemory cm = new CacheMemory(
					i, Parameter.NUMBER_OF_REPLICA, param, Parameter.BLOCK_SIZE);
			cacheMemories.put(i, cm);
		}

		IAssignor assignor =
			new BalancedAssignor(numcm);

		return new RAPoSDACacheMemoryManager(
				cacheMemories, assignor, Parameter.NUMBER_OF_REPLICA);
	}

	private RAPoSDACacheDiskManager getCDM() {
		int numcd = Parameter.NUMBER_OF_CACHE_DISKS;
		HashMap<Integer, CacheDisk> cacheDisks = new HashMap<Integer, CacheDisk>();

		HDDParameter param = new HDDParameter(
				Parameter.HDD_SIZE,
				Parameter.HDD_NUMBER_OF_PLATTER,
				Parameter.HDD_RPM,
				Parameter.HDD_CACHE_SIZE,
				Parameter.HDD_TRANSFER_RATE,
				Parameter.HDD_SECTORS_PER_TRACK,
				Parameter.HDD_FULL_STROKE_SEEK_TIME,
				Parameter.HDD_HEAD_SWITCH_OVERHEAD,
				Parameter.HDD_COMMAND_OVERHEAD
		);

		CacheDiskStateManager cdstm =
			new CacheDiskStateManager(
					new DiskStateParameter(
							Parameter.HDD_ACTIVE_POWER,
							Parameter.HDD_IDLE_POWER,
							Parameter.HDD_STANDBY_POWER,
							Parameter.HDD_SPINDOWN_ENERGY,
							Parameter.HDD_SPINDOWN_ENERGY,
							Parameter.HDD_SPINDOWN_TIME,
							Parameter.HDD_SPINUP_TIME
					)
			);

		for (int i=0; i < numcd; i++) {
			CacheDisk cd = new CacheDisk(i, Parameter.BLOCK_SIZE, param, cdstm);
			cacheDisks.put(i, cd);
		}

		return new RAPoSDACacheDiskManager(numcd, cacheDisks);
	}

	private RAPoSDADataDiskManager getDDM() {
		int numdd =
			Parameter.NUMBER_OF_CACHE_MEMORIES
			* Parameter.NUBER_OF_DISKS_PER_CACHE_GROUP;
		int numRep = Parameter.NUMBER_OF_REPLICA;
		HashMap<Integer, DataDisk> dataDisks = new HashMap<Integer, DataDisk>();

		HDDParameter param = new HDDParameter(
				Parameter.HDD_SIZE,
				Parameter.HDD_NUMBER_OF_PLATTER,
				Parameter.HDD_RPM,
				Parameter.HDD_CACHE_SIZE,
				Parameter.HDD_TRANSFER_RATE,
				Parameter.HDD_SECTORS_PER_TRACK,
				Parameter.HDD_FULL_STROKE_SEEK_TIME,
				Parameter.HDD_HEAD_SWITCH_OVERHEAD,
				Parameter.HDD_COMMAND_OVERHEAD
		);

		DataDiskStateManager ddstm =
			new DataDiskStateManager(
					Parameter.SPINDOWN_THRESHOLD,
					new DiskStateParameter(
							Parameter.HDD_ACTIVE_POWER,
							Parameter.HDD_IDLE_POWER,
							Parameter.HDD_STANDBY_POWER,
							Parameter.HDD_SPINDOWN_ENERGY,
							Parameter.HDD_SPINDOWN_ENERGY,
							Parameter.HDD_SPINDOWN_TIME,
							Parameter.HDD_SPINUP_TIME
					)
			);

		for (int i=0; i < numdd; i++) {
			DataDisk dd = new DataDisk(i, param, ddstm);
			dataDisks.put(i, dd);
		}
		return new RAPoSDADataDiskManager(numdd, numRep, dataDisks);
	}

	private Client getClient() {
		return new Client(Parameter.WORKLOAD_FILE_PATH);
	}

	private double run(Client client, RAPoSDAStorageManager sm) {
		client.registerInitialData(sm);
		return client.run(sm);
	}

	private void close(double closeTime, RAPoSDAStorageManager sm) {
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
		System.out.println("------------------");
		System.out.printf("Simulation Time: %.3f\n", closeTime);
		System.out.printf("Total Energy: %.4f\n", RAPoSDAStats.getTotalEnergyConsumption());
		System.out.printf("Avg. Response Time: %.6f\n", RAPoSDAStats.getAverageResponseTime());
		System.out.printf("Spindown count: %d\n", RAPoSDAStats.getSpindownCount());
		System.out.printf("Spinup   count: %d\n", RAPoSDAStats.getSpinupCount());
		System.out.printf("Avg. data disk response time: %.6f\n", RAPoSDAStats.getAverageDataDiskResponseTime());
		System.out.printf("data disk access count : %,d\n", RAPoSDAStats.getNumberOfDataDiskAccesses());
		System.out.printf("Avg. cache disk response time: %.6f\n", RAPoSDAStats.getAverageCacheDiskResponseTime());
		System.out.printf("cache disk access count : %,d\n", RAPoSDAStats.getNumberOfCacheDiskAccesses());
	}

	public static void main(String[] args) {
		// TODO parse command line options

		long simStart = System.currentTimeMillis();

		Simulator sim = new Simulator();
		sim.displayStartMessage();

		Client client = sim.getClient();
		RAPoSDAStorageManager sm = sim.getSM();

		double exeTime = sim.run(client, sm);

		sim.close(exeTime, sm);

		long simEnd = System.currentTimeMillis();
		sim.displayTerminateMessage(simEnd - simStart);
	}
}
