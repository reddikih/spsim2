package sim.storage.manager.cmm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import sim.Block;
import sim.storage.CacheResponse;
import sim.storage.manager.cmm.assignor.IAssignor;
import sim.storage.util.ReplicaLevel;

public class RAPoSDACacheMemoryManager {

	private HashMap<Integer, CacheMemory> cacheMemories;
	private IAssignor assignor;
	private int numReplica;

	public RAPoSDACacheMemoryManager(
			HashMap<Integer, CacheMemory> cacheMemories,
			IAssignor assignor,
			int numReplica) {
		this.cacheMemories = cacheMemories;
		this.assignor = assignor;
		this.numReplica = numReplica;
	}

	public CacheResponse read(Block block) {
		int assignedMemory = assignor.assign(
				block.getPrimaryDiskId(),
				block.getRepLevel().getValue());

		CacheMemory cm = cacheMemories.get(assignedMemory);
		return cm.read(block);
	}

//	public List<RAPoSDACacheWriteResponse> write(Block block) {
	public RAPoSDACacheWriteResponse write(Block block) {

//		List<RAPoSDACacheWriteResponse> result =
//			new ArrayList<RAPoSDACacheWriteResponse>();

		// TODO this process may have SM rather than CMM.
//		block.setRepLevel(ReplicaLevel.ZERO);
//		block.setOwnerDiskId(block.getPrimaryDiskId());
//		List<Block> writeBlocks = new ArrayList<Block>();
//		writeBlocks.add(block);
//		int repCounter = numReplica;
//		for (ReplicaLevel repLevel : ReplicaLevel.values()) {
//			repCounter--;
//			if (repLevel.getValue() == 0) continue;
//			if (repCounter < 0) break;
//
//			// make replica blocks
//			Block replica = new Block(block.getId(),
//									  block.getAccessTime(),
//									  block.getPrimaryDiskId());
//			replica.setRepLevel(repLevel);
//			replica.setOwnerDiskId(
//					replica.getPrimaryDiskId() + replica.getRepLevel().getValue() );
//
//			writeBlocks.add(replica);
//		}

		// assign cache memory and write to the cache memory.
//		for (Block b : writeBlocks) {
//			int assignedMemory = assignor.assign(
//									b.getPrimaryDiskId(),
//									b.getRepLevel().getValue());
//
//			// write to corresponding cache memory
//			CacheMemory cm = cacheMemories.get(assignedMemory);
//			result.add(cm.write(b));
//		}

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
