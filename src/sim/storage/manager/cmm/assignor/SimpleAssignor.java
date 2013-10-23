package sim.storage.manager.cmm.assignor;


public class SimpleAssignor implements IAssignor {
	
	@Override
	public int assign(int primaryDiskId, int replicaLevel) {
		return replicaLevel;
	}
}
