package sim.storage.manager.cmm;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sim.Block;
import sim.storage.CacheParameter;
import sim.storage.CacheResponse;
import sim.storage.util.ReplicaLevel;

public class CacheMemory {

	private int id;
	private CacheParameter parameter;
	private HashMap<ReplicaLevel, Region> regions;

	private static Logger logger = LoggerFactory.getLogger(CacheMemory.class);


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

		// cache memory read log.
		logger.trace(
				String.format(
						"CM[%d] Regin:%d read blockId:%d hit:%d",
						this.id,
						block.getRepLevel().getValue(),
						block.getId(),
						result.equals(Block.NULL) ? 0 : 1
						));

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

		// cache memory write log.
		logger.trace(
				String.format(
						"CM[%d] Regin:%d write blockId:%d overflowLength:%d",
						this.id,
						block.getRepLevel().getValue(),
						block.getId(),
						response.getOverflows().length));

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
