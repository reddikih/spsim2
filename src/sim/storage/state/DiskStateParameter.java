package sim.storage.state;

public class DiskStateParameter {

	private final double activePower;
	private final double idlePower;
	private final double standbyPower;
	/** HDD spindown energy represents by joule(w/s) */
	private final double spindownEnergy;
	private final double spinupEnergy;
	private final double spindownTime;
	private final double spinupTime;

	public DiskStateParameter(
			double activePower,
			double idlePower,
			double standbyPower,
			double spindownEnergy,
			double spinupEnergy,
			double spindownTime,
			double spinupTime) {
		this.activePower = activePower;
		this.idlePower = idlePower;
		this.standbyPower = standbyPower;
		this.spindownEnergy = spindownEnergy;
		this.spinupEnergy = spinupEnergy;
		this.spindownTime = spindownTime;
		this.spinupTime = spinupTime;
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
