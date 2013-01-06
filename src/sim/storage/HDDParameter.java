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

	public HDDParameter(
			long hddSize,
			int numberOfPlatters,
			int rpm,
			long cacheSize,
			long transferRate,
			int sectorsPerTrack,
			double fullStrokeSeekTime,
			double headSwitchOverhead,
			double commandOverhead) {

		this.hddSize = hddSize;
		this.numberOfPlatters = numberOfPlatters;
		this.rpm = rpm;
		this.cacheSize = cacheSize;
		this.transferRate = transferRate;
		this.sectorsPerTrack = sectorsPerTrack;
		this.fullStrokeSeekTime = fullStrokeSeekTime;
		this.headSwitchOverhead = headSwitchOverhead;
		this.commandOverhead = commandOverhead;
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
}
