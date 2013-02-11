package sim.storage.manager.cmm;

import java.util.HashMap;
import java.util.List;

import sim.Block;
import sim.storage.CacheResponse;
import sim.storage.manager.cmm.assignor.IAssignor;
import sim.storage.util.DiskInfo;
import sim.storage.util.ReplicaLevel;

public class RAPoSDACacheMemoryManager implements ICacheMemoryManager {

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

	@Override
	public CacheResponse read(Block block) {
		CacheResponse result = null;
		int primDiskId = block.getPrimaryDiskId();

		for (ReplicaLevel repLevel : ReplicaLevel.values()) {
			if (repLevel.getValue() >= numReplica) break;
			int assignedMemory =
				assignor.assign(primDiskId, repLevel.getValue());
			CacheMemory cm = cacheMemories.get(assignedMemory);
			assert cm != null;
			Block retrieveBlock = new Block(
					block.getId(),
					block.getAccessTime(),
					block.getPrimaryDiskId());
			retrieveBlock.setRepLevel(repLevel);
			result = cm.read(retrieveBlock);
			if (result.getResult().equals(block)) break;
		}
		assert result != null;
		return result;
	}

	@Override
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

	@Override
	public CacheResponse remove(Block toRemove) {
		int assignedMemory = assignor.assign(
				toRemove.getPrimaryDiskId(),
				toRemove.getRepLevel().getValue());

		CacheMemory cm = cacheMemories.get(assignedMemory);
		return cm.remove(toRemove);
	}

	public DiskInfo getMaxBufferDisk(List<DiskInfo> diskInfos) {
		DiskInfo result = null;
		int maxBuffer = Integer.MIN_VALUE;
		for (DiskInfo diskInfo : diskInfos) {
			int cmIdx = assignor.assign(
					diskInfo.getDiskId() - diskInfo.getRepLevel().getValue(),
					diskInfo.getRepLevel().getValue());
			CacheMemory cm = cacheMemories.get(cmIdx);
			assert cm != null;
			Region region = cm.getRegion(diskInfo.getRepLevel());
			if (maxBuffer < region.getBufferLenght()) {
				maxBuffer = region.getBufferLenght();
				result = diskInfo;
			}
		}
		return result;
	}
}
