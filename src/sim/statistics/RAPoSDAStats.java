package sim.statistics;

public class RAPoSDAStats {

	public static enum STAT_TYPE {
		ACTIVE_ENERGY,
		IDLE_ENERGY,
		STANDBY_ENERGY,
		SPINDOWN_ENERGY,
		SPINUP_ENERGY,
		RESPONSE_TIME,
	}

	private static double totalActiveEnergy;
	private static double totalIdleEnergy;
	private static double totalStandbyEnergy;
	private static double totalSpindownEnergy;
	private static double totalSpinupEnergy;

	private static double totalResponseTime;
	private static double totalRequests;

	public static void addEnergy(double added, STAT_TYPE type) {
		switch(type) {
		case ACTIVE_ENERGY:
			totalActiveEnergy += added;
		case IDLE_ENERGY:
			totalIdleEnergy += added;
		case STANDBY_ENERGY:
			totalStandbyEnergy += added;
		case SPINUP_ENERGY:
			totalSpinupEnergy += added;
		case SPINDOWN_ENERGY:
			totalSpindownEnergy += added;
		default:
			throw new IllegalArgumentException("STAT_TYPE is invalid.");
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
