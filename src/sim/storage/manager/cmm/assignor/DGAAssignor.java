package sim.storage.manager.cmm.assignor;

public class DGAAssignor implements IAssignor {

	private int numCacheMemory;
	private int numDisksPerGroup;

	public DGAAssignor(int numCacheMemory, int numDisksPerGroup) {
		this.numCacheMemory = numCacheMemory;
		this.numDisksPerGroup = numDisksPerGroup;
	}

	@Override
	public int assign(int primaryDiskId, int replicaLevel) {
		int result = -1;
		if (replicaLevel == 0) {
			result = (int)Math.floor(primaryDiskId / numDisksPerGroup) % numCacheMemory;
		} else {
			result = (assign(primaryDiskId, replicaLevel - 1) + 1) % numCacheMemory;
		}
		return result;
	}
}
