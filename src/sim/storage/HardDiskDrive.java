package sim.storage;

import sim.Block;

public class HardDiskDrive {

	private final static int SECTOR_SIZE = 512;
	private final int id;

	private double lastArrivalTime;
	private double lastResponseTime;

	private HDDParameter parameters;
	private DiskCache diskCache;

	public HardDiskDrive(int id, HDDParameter parameter) {
		this(id, parameter, null);
	}

	public HardDiskDrive(int id, HDDParameter parameter, DiskCache diskCache) {
		this.id = id;
		this.parameters = parameter;
		this.diskCache = diskCache;
	}

	public double read(Block[] blocks) {
		return 0.0;
	}

	public double write(Block[] blocks) {
		double response = 0.0;
		for (Block block : blocks) {
			response += diskAccess(block.BLOCK_SIZE, block.getAccessTime());
		}
		return response;
	}

	private double diskAccess(int size, double arrivalTime) {
		double serviceTime = calculateServiceTime(size);
		double queueingTime = calculateQueueingTime(arrivalTime);
		double responseTime = serviceTime + queueingTime;

		updateAccessParameter(arrivalTime, responseTime);

		return responseTime;
	}

	private double calculateServiceTime(int size) {
		int block_len = (int)Math.ceil(size / SECTOR_SIZE);
		double fsst = parameters.getFullStrokeSeekTime();         // full stroke seek time.
		double fdrt = 60.0 / parameters.getRpm();                 // full disk rotation time
		int sec_per_track = parameters.getSectorsPerTrack();      // sectors per track
		double overhead = parameters.getHeadSwitchOverhead() + parameters.getCommandOverhead(); // overhead time
		double transfer_rate = 1.0 / ((double)parameters.getTransferRate() / SECTOR_SIZE); // sectors/s

		return (fsst/2) + (fdrt/2) + fdrt*(block_len/sec_per_track) + overhead + transfer_rate;
	}

	private double calculateQueueingTime(double arrivalTime) {
		double lastAccessTime = lastArrivalTime + lastResponseTime;
		double queueingTime = arrivalTime < lastAccessTime ? lastAccessTime - arrivalTime : 0.0;
		return queueingTime;
	}

	private void updateAccessParameter(double arrivalTime, double responseTime) {
		this.lastArrivalTime = arrivalTime;
		this.lastResponseTime = responseTime;
	}

}
