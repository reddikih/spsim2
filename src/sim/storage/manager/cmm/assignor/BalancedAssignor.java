package sim.storage.manager.cmm.assignor;

public class BalancedAssignor implements IAssignor {

	private int numCacheMemory;

	public BalancedAssignor(int numCacheMemory) {
		this.numCacheMemory = numCacheMemory;
	}

	@Override
	public int assign(int primaryDiskId, int replicaLevel) {
		int result = -1;
		if (replicaLevel == 0) {
			result = primaryDiskId % numCacheMemory;
		} else {
			result = (assign(primaryDiskId, replicaLevel - 1) + 1) % numCacheMemory;
		}
		return result;
	}
}
