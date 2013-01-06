package sim.storage.manager.cmm;

import java.util.HashMap;

import sim.Block;
import sim.storage.CacheParameter;
import sim.storage.CacheResponse;
import sim.storage.util.ReplicaLevel;

public class CacheMemory {

	private int id;
	private CacheParameter parameter;
	private HashMap<ReplicaLevel, Region> regions;


	public CacheMemory(int id, int numReplica, CacheParameter parameter, int blockSize) {
		this.id = id;
		this.parameter = parameter;
		this.regions = new HashMap<ReplicaLevel, Region>();

		setUpRegions(numReplica, blockSize);
	}

	private void setUpRegions(int numReplica, int blockSize) {
		// cache memory capacity(number of blocks) per region
		int maxEntries = (int)Math.ceil(this.parameter.getCapacity() / blockSize / numReplica);
		for (ReplicaLevel repLevel : ReplicaLevel.values()) {
			if (numReplica <= 0) break;
			regions.put(repLevel, new Region(maxEntries));
			numReplica--;
		}
	}

	public CacheResponse read(Block block) {
		Region region = regions.get(block.getRepLevel());
		assert region != null;

		Block result = region.read(block);
		CacheResponse response =
			new CacheResponse(parameter.getLatency(), result);

		return response;
	}

	public RAPoSDACacheWriteResponse write(Block block) {
		Region region = regions.get(block.getRepLevel());
		assert region != null;

		RAPoSDACacheWriteResponse response = null;

		Block result = region.write(block);
		if (result.equals(Block.NULL)) { // overflow
			response = new RAPoSDACacheWriteResponse(
							parameter.getLatency(),
							region.getMaxBufferLenghtDiskId(),
							region.getBlocks());
		} else {
			response = new RAPoSDACacheWriteResponse(
							parameter.getLatency(),
							region.getMaxBufferLenghtDiskId(),
							region.getEmptyBlocks());
		}

		return response;
	}

	public CacheResponse remove(Block block) {
		Region region = regions.get(block.getRepLevel());
		assert region != null;
		return new CacheResponse(parameter.getLatency(), region.remove(block));
	}

	public int getId() {
		return id;
	}

	public Region getRegion(ReplicaLevel repLevel) {
		return regions.get(repLevel);
	}

}
