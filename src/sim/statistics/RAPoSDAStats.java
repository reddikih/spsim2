package sim.statistics;

import sim.storage.util.DiskState;

public class RAPoSDAStats {

	private static double totalActiveEnergy;
	private static double totalIdleEnergy;
	private static double totalStandbyEnergy;
	private static double totalSpindownEnergy;
	private static double totalSpinupEnergy;

	private static int spindownCount;
	private static int spinupCount;

	private static double totalResponseTime;
	private static int totalRequests;

	private static double totalDataDiskResponseTime;
	private static int totalDataDiskAccesses;

	private static double totalCacheDiskResponseTime;
	private static int totalCacheDiskAccesses;

	public static void addEnergy(double added, DiskState type) {
		switch(type) {
		case ACTIVE:
			totalActiveEnergy += added;
			break;
		case IDLE:
			totalIdleEnergy += added;
			break;
		case STANDBY:
			totalStandbyEnergy += added;
			break;
		case SPINUP:
			totalSpinupEnergy += added;
			break;
		case SPINDOWN:
			totalSpindownEnergy += added;
			break;
		default:
			throw new IllegalArgumentException("state is invalid." + type);
		}
	}

	public static void addResponseTime(double added) {
		totalResponseTime += added;
		totalRequests++;
	}

	// ignoring seek count.
	public static void addDataDiskResponseTime(double added) {
		totalDataDiskResponseTime += added;
		totalDataDiskAccesses++;
	}

	public static void addCacheDiskResponseTime(double added) {
		totalCacheDiskResponseTime += added;
		totalCacheDiskAccesses++;
	}

	public static void incrementSpindownCount() {
		spindownCount++;
	}

	public static void incrementSpinupCount() {
		spinupCount++;
	}

	public static int getSpindownCount() {
		return spindownCount;
	}

	public static int getSpinupCount() {
		return spinupCount;
	}

	public static double getTotalEnergyConsumption() {
		return
		totalActiveEnergy +
		totalIdleEnergy +
		totalStandbyEnergy +
		totalSpinupEnergy +
		totalSpindownEnergy;
	}

	public static double getAverageResponseTime() {
		if (totalRequests == 0) return 0.0;
		return totalResponseTime / totalRequests;
	}

	public static int getNumberOfRequests() {
		return totalRequests;
	}

	public static double getAverageDataDiskResponseTime() {
		if (totalDataDiskAccesses == 0) return 0.0;
		return totalDataDiskResponseTime / totalDataDiskAccesses;
	}

	public static int getNumberOfDataDiskAccesses() {
		return totalDataDiskAccesses;
	}

	public static double getAverageCacheDiskResponseTime() {
		if (totalCacheDiskAccesses == 0) return 0.0;
		return totalCacheDiskResponseTime / totalCacheDiskAccesses;
	}

	public static int getNumberOfCacheDiskAccesses() {
		return totalCacheDiskAccesses;
	}

}
