package sim.storage;

public class HDDParameter {

	/** HDD size. 2TB by spec. */
	private final long hddSize;
	private final int numberOfPlatters;
	private final int rpm;
	/** HDD disk cache size. 32MB by spec. */
	private final long cacheSize;
	private final long transferRate;
	private final int sectorsPerTrack;
	private final double fullStrokeSeekTime;
	private final double headSwitchOverhead;
	private final double commandOverhead;
	private final double activePower;
	private final double idlePower;
	private final double standbyPower;
	/** HDD spindown energy represents by joule(w/s) */
	private final double spindownEnergy;
	private final double spinupEnergy;
	private final double spindownTime;
	private final double spinupTime;

	public HDDParameter(
			long hddSize,
			int numberOfPlatters,
			int rpm,
			long cacheSize,
			long transferRate,
			int sectorsPerTrack,
			double fullStrokeSeekTime,
			double headSwitchOverhead,
			double commandOverhead,
			double activePower,
			double idlePower,
			double standbyPower,
			double spindownEnergy,
			double spinupEnergy,
			double spindownTime,
			double spinupTime) {

		this.hddSize = hddSize;
		this.numberOfPlatters = numberOfPlatters;
		this.rpm = rpm;
		this.cacheSize = cacheSize;
		this.transferRate = transferRate;
		this.sectorsPerTrack = sectorsPerTrack;
		this.fullStrokeSeekTime = fullStrokeSeekTime;
		this.headSwitchOverhead = headSwitchOverhead;
		this.commandOverhead = commandOverhead;
		this.activePower = activePower;
		this.idlePower = idlePower;
		this.standbyPower = standbyPower;
		this.spindownEnergy = spindownEnergy;
		this.spinupEnergy = spinupEnergy;
		this.spindownTime = spindownTime;
		this.spinupTime = spinupTime;
	}

	public long getHddSize() {
		return hddSize;
	}

	public int getNumberOfPlatters() {
		return numberOfPlatters;
	}

	public int getRpm() {
		return rpm;
	}

	public long getCacheSize() {
		return cacheSize;
	}

	public long getTransferRate() {
		return transferRate;
	}

	public int getSectorsPerTrack() {
		return sectorsPerTrack;
	}

	public double getFullStrokeSeekTime() {
		return fullStrokeSeekTime;
	}

	public double getHeadSwitchOverhead() {
		return headSwitchOverhead;
	}

	public double getCommandOverhead() {
		return commandOverhead;
	}

	public double getActivePower() {
		return activePower;
	}

	public double getIdlePower() {
		return idlePower;
	}

	public double getStandbyPower() {
		return standbyPower;
	}

	public double getSpindownEnergy() {
		return spindownEnergy;
	}

	public double getSpinupEnergy() {
		return spinupEnergy;
	}

	public double getSpindownTime() {
		return spindownTime;
	}

	public double getSpinupTime() {
		return spinupTime;
	}

}
