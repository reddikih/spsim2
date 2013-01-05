package sim.storage.manager.cmm;

import java.util.HashMap;

import sim.Block;
import sim.storage.CacheResponse;
import sim.storage.manager.cmm.assignor.IAssignor;

public class RAPoSDACacheMemoryManager {

	private HashMap<Integer, CacheMemory> cacheMemories;
	private IAssignor assignor;

	public RAPoSDACacheMemoryManager(
			HashMap<Integer, CacheMemory> cacheMemories,
			IAssignor assignor,
			int numReplica) {
		this.cacheMemories = cacheMemories;
		this.assignor = assignor;
	}

	public CacheResponse read(Block block) {
		int assignedMemory = assignor.assign(
				block.getPrimaryDiskId(),
				block.getRepLevel().getValue());

		CacheMemory cm = cacheMemories.get(assignedMemory);
		return cm.read(block);
	}

	public RAPoSDACacheWriteResponse write(Block block) {
		RAPoSDACacheWriteResponse response;
		int assignedMemory = assignor.assign(
								block.getPrimaryDiskId(),
								block.getRepLevel().getValue());

		// write to corresponding cache memory
		CacheMemory cm = cacheMemories.get(assignedMemory);
		response = cm.write(block);

		return response;
	}

	public CacheResponse remove(Block toRemove) {
		int assignedMemory = assignor.assign(
				toRemove.getPrimaryDiskId(),
				toRemove.getRepLevel().getValue());

		CacheMemory cm = cacheMemories.get(assignedMemory);
		return cm.remove(toRemove);
	}
}
