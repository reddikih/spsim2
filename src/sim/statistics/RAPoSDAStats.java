package sim.statistics;

import sim.storage.util.DiskState;
import sim.storage.util.RequestType;

public class RAPoSDAStats {

	private static long readRequestCount;
	private static long writeRequestCount;

	private static long readBlockAccessCount;
	private static long writeBlockAccessCount;

	private static long cacheMemoryReadCount;
	private static long cacheMemoryReadHitCount;
	private static long cacheMemoryWriteCount;
	private static long cacheMemoryWriteHitCount;


	private static double totalActiveEnergy;
	private static double totalIdleEnergy;
	private static double totalStandbyEnergy;
	private static double totalSpindownEnergy;
	private static double totalSpinupEnergy;
	
	private static double activeStateTime;
	private static double idleStateTime;
	private static double standbyStateTime;
	private static double spindownStateTime;
	private static double spinupStateTime;
	
	private static double activeStateCount;
	private static double idleStateCount;
	private static double standbyStateCount;
	private static double spindownStateCount;
	private static double spinupStateCount;

	private static int spindownCount;
	private static int spinupCount;

	private static int overflowCount;

	private static double totalResponseTime;
	private static int totalRequests;

	private static double totalDataDiskResponseTime;
	private static long totalDataDiskBlockAccesses;

	private static long dataDiskReadCount;
	private static long dataDiskWriteCount;

	private static double totalCacheDiskResponseTime;
	private static long actualCacheDiskBlockAccesseCount;

	private static long cacheDiskReadCount;
	private static long cacheDiskWriteCount;
	private static long cacheDiskReadHitCount;
	private static long cacheDiskWriteHitCount;

	public static void addEnergy(double added, DiskState type, double time) {
		switch(type) {
		case ACTIVE:
			totalActiveEnergy += added;
			activeStateTime += time;
			activeStateCount++;
			break;
		case IDLE:
			totalIdleEnergy += added;
			idleStateTime += time;
			idleStateCount++;
			break;
		case STANDBY:
			totalStandbyEnergy += added;
			standbyStateTime += time;
			standbyStateCount++;
			break;
		case SPINUP:
			totalSpinupEnergy += added;
			spinupStateTime += time;
			spinupStateCount++;
			break;
		case SPINDOWN:
			totalSpindownEnergy += added;
			spindownStateTime += time;
			spindownStateCount++;
			break;
		default:
			throw new IllegalArgumentException("state is invalid." + type);
		}
	}

	public static void incrementRequest(RequestType reqType) {
		switch (reqType) {
		case READ:
			readRequestCount++; break;
		case WRITE:
			writeRequestCount++; break;
		}
	}

	public static long getRequestCount(RequestType reqType) {
		return RequestType.READ.equals(reqType)
		? readRequestCount : writeRequestCount;
	}

	public static void incrementBlockAccessCount(RequestType reqType) {
		switch (reqType) {
		case READ:
			readBlockAccessCount++; break;
		case WRITE:
			writeBlockAccessCount++; break;
		}
	}

	public static long getBlockAccessCount(RequestType reqType) {
		return RequestType.READ.equals(reqType)
		? readBlockAccessCount : writeBlockAccessCount;
	}

	public static void incrementCacheMemoryAccessCount(RequestType reqType, boolean isHit) {
		switch (reqType) {
		case READ:
			cacheMemoryReadCount++;
			if (isHit) cacheMemoryReadHitCount++;
			break;
		case WRITE:
			cacheMemoryWriteCount++;
			if (isHit) cacheMemoryWriteHitCount++;
			break;
		}
	}

	public static long getCacheMemoryAccessCount(RequestType reqType) {
		return RequestType.READ.equals(reqType)
		? cacheMemoryReadCount : cacheMemoryWriteCount;
	}

	public static double getCacheMemoryHitRatio(RequestType reqType) {
		double result = -1.0;
		if (RequestType.READ.equals(reqType)) {
			result = cacheMemoryReadCount == 0
			? 0.0 : (double)cacheMemoryReadHitCount / cacheMemoryReadCount;
		} else if (RequestType.WRITE.equals(reqType)) {
			result = cacheMemoryWriteCount == 0
			? 0.0 : (double)cacheMemoryWriteHitCount / cacheMemoryWriteCount;
		} else {
			throw new IllegalArgumentException("request type is invalid.");
		}
		return result;
	}

	public static void addResponseTime(double added) {
		totalResponseTime += added;
		totalRequests++;
	}

	// ignoring seek count.
	public static void addDataDiskResponseTime(double added) {
		totalDataDiskResponseTime += added;
		totalDataDiskBlockAccesses++;
	}

	public static void incrementDataDiskAccessCount(RequestType reqType) {
		switch (reqType) {
		case READ:
			dataDiskReadCount++; break;
		case WRITE:
			dataDiskWriteCount++; break;
		}
	}

	public static long getDataDiskAccessCount(RequestType reqType) {
		long result = -1;
		if (RequestType.READ.equals(reqType)) result = dataDiskReadCount;
		else if (RequestType.WRITE.equals(reqType)) result = dataDiskWriteCount;
		return result;
	}

	public static void addCacheDiskResponseTime(double added) {
		totalCacheDiskResponseTime += added;
		actualCacheDiskBlockAccesseCount++;
	}


	public static void incrementCacheDiskAccessCount(
			RequestType reqType, boolean isHit) {
		if (RequestType.READ.equals(reqType)) {
			cacheDiskReadCount++;
			if (isHit) cacheDiskReadHitCount++;
		} else if (RequestType.WRITE.equals(reqType)){
			cacheDiskWriteCount++;
			if (isHit) cacheDiskWriteHitCount++;
		}
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

	public static void incrementOverflowCount() {
		overflowCount++;
	}

	public static int getOverflowCount() {
		return overflowCount;
	}

	public static double getTotalEnergyConsumption() {
		return
		totalActiveEnergy +
		totalIdleEnergy +
		totalStandbyEnergy +
		totalSpinupEnergy +
		totalSpindownEnergy;
	}

	public static double getTotalEnergyConsumptionByState(DiskState state) {
		double result = -1.0;
		if (DiskState.ACTIVE.equals(state)) result = totalActiveEnergy;
		else if (DiskState.IDLE.equals(state)) result = totalIdleEnergy;
		else if (DiskState.STANDBY.equals(state)) result = totalStandbyEnergy;
		else if (DiskState.SPINUP.equals(state)) result = totalSpinupEnergy;
		else if (DiskState.SPINDOWN.equals(state)) result = totalSpindownEnergy;
		return result;
	}

	public static double getAverageResponseTime() {
		if (totalRequests == 0) return 0.0;
		return totalResponseTime / totalRequests;
	}

	public static int getNumberOfRequests() {
		return totalRequests;
	}

	public static double getAverageDataDiskResponseTime() {
		if (totalDataDiskBlockAccesses == 0) return 0.0;
		return totalDataDiskResponseTime / totalDataDiskBlockAccesses;
	}

	public static long getNumberOfDataDiskBlockAccesses() {
		return totalDataDiskBlockAccesses;
	}

	public static double getAverageCacheDiskResponseTime() {
		if (actualCacheDiskBlockAccesseCount == 0) return 0.0;
		return totalCacheDiskResponseTime / actualCacheDiskBlockAccesseCount;
	}

	public static long getActualCacheDiskBlockAccesseCount() {
		return actualCacheDiskBlockAccesseCount;
	}

	public static long getCacheDiskAccessCount(RequestType reqType) {
		return RequestType.READ.equals(reqType)
		? cacheDiskReadCount : cacheDiskWriteCount;
	}

	public static double getCacheDiskHitRatio(RequestType reqType) {
		double result = -1.0;
		if (RequestType.READ.equals(reqType)) {
			result = cacheDiskReadCount == 0
			? 0.0 : (double)cacheDiskReadHitCount / cacheDiskReadCount;
		} else if (RequestType.WRITE.equals(reqType)) {
			result = cacheDiskWriteCount == 0
			? 0.0 : (double)cacheDiskWriteHitCount / cacheDiskWriteCount;
		}
		return result;
	}


	public static void showStatistics(double closeTime) {
		System.out.println("------------------");
		System.out.printf("Simulation Time: %.3f\n", closeTime);
		System.out.printf("Total Energy(totaltime : avg time): %,.4f\n", getTotalEnergyConsumption());
		System.out.printf("  ACTIVE   : %,.4f(%,.4f : %,.4f)\n", getTotalEnergyConsumptionByState(DiskState.ACTIVE), activeStateTime, activeStateCount != 0.0 ? activeStateTime / activeStateCount : -1.0);
		System.out.printf("  IDLE     : %,.4f(%,.4f : %,.4f)\n", getTotalEnergyConsumptionByState(DiskState.IDLE), idleStateTime, idleStateCount != 0.0 ? idleStateTime / idleStateCount : -1.0);
		System.out.printf("  STANDBY  : %,.4f(%,.4f : %,.4f)\n", getTotalEnergyConsumptionByState(DiskState.STANDBY), standbyStateTime, standbyStateCount != 0.0 ? standbyStateTime / standbyStateCount : -1.0);
		System.out.printf("  SPINDOWN : %,.4f(%,.4f : %,.4f)\n", getTotalEnergyConsumptionByState(DiskState.SPINDOWN), spindownStateTime, spindownStateCount != 0.0 ? spindownStateTime / spindownStateCount : -1.0);
		System.out.printf("  SPINUP   : %,.4f(%,.4f : %,.4f)\n", getTotalEnergyConsumptionByState(DiskState.SPINUP), spinupStateTime, spinupStateCount != 0.0 ? spinupStateTime / spinupStateCount : -1.0);
		System.out.printf("Avg. Response Time: %.6f\n", getAverageResponseTime());
		System.out.printf("Total Request count  : %,d\n", getRequestCount(RequestType.READ) + getRequestCount(RequestType.WRITE));
		System.out.printf("  Read Request count : %,d(%,d)\n", getRequestCount(RequestType.READ), getBlockAccessCount(RequestType.READ));
		System.out.printf("  Write Request count: %,d(%,d)\n", getRequestCount(RequestType.WRITE), getBlockAccessCount(RequestType.WRITE));
		System.out.printf("Cache memory read count (hit ratio): %,d(%.4f)\n", getCacheMemoryAccessCount(RequestType.READ), getCacheMemoryHitRatio(RequestType.READ));
		System.out.printf("Cache memory write count(hit ratio): %,d(%.4f)\n", getCacheMemoryAccessCount(RequestType.WRITE), getCacheMemoryHitRatio(RequestType.WRITE));
		System.out.printf("Avg. data disk response time : %.6f\n", getAverageDataDiskResponseTime());
		System.out.printf("data disk access count         : %,d\n", getDataDiskAccessCount(RequestType.READ) + getDataDiskAccessCount(RequestType.WRITE));
		System.out.printf("  data disk read access count  : %,d\n", getDataDiskAccessCount(RequestType.READ));
		System.out.printf("  data disk write access count : %,d\n", getDataDiskAccessCount(RequestType.WRITE));
		System.out.printf("Avg. cache disk response time: %.6f\n", getAverageCacheDiskResponseTime());
		System.out.printf("cache disk access count(actual)         : %,d(%,d)\n", getCacheDiskAccessCount(RequestType.READ) + getCacheDiskAccessCount(RequestType.WRITE), getActualCacheDiskBlockAccesseCount());
		System.out.printf("  cache disk read access count(hit ratio) : %,d(%.4f)\n", getCacheDiskAccessCount(RequestType.READ), getCacheDiskHitRatio(RequestType.READ));
		System.out.printf("  cache disk write access count(hit ratio): %,d(%.4f)\n", getCacheDiskAccessCount(RequestType.WRITE), getCacheDiskHitRatio(RequestType.WRITE));
		System.out.printf("Spindown count: %,d\n", getSpindownCount());
		System.out.printf("Spinup   count: %,d\n", getSpinupCount());
		System.out.printf("Buffer overflow count: %,d\n", getOverflowCount());
	}

}
