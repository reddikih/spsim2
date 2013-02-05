package sim.statistics;

import sim.storage.util.DiskState;

public class RAPoSDAStats {

	private static double totalActiveEnergy;
	private static double totalIdleEnergy;
	private static double totalStandbyEnergy;
	private static double totalSpindownEnergy;
	private static double totalSpinupEnergy;

	private static double totalResponseTime;
	private static double totalRequests;

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

}
