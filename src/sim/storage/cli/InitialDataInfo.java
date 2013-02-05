package sim.storage.cli;

public class InitialDataInfo {

	private int numberOfFiles;
	private int lowerBound;
	private int upperBound;

	public InitialDataInfo(int numberOfFiles, int lowerBound, int upperBound) {
		this.numberOfFiles = numberOfFiles;
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
	}

	public int getNumberOfFiles() {
		return numberOfFiles;
	}

	public int getLowerBound() {
		return lowerBound;
	}

	public int getUpperBound() {
		return upperBound;
	}

}
