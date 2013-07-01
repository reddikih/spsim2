package sim.storage.manager.cmm;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sim.Block;
import sim.statistics.RAPoSDAStats;
import sim.storage.CacheParameter;
import sim.storage.CacheResponse;
import sim.storage.util.ReplicaLevel;
import sim.storage.util.RequestType;

public abstract class CacheMemory {

	protected int id;
	protected CacheParameter parameter;
	protected HashMap<ReplicaLevel, Region> regions;

	private static Logger logger = LoggerFactory.getLogger(CacheMemory.class);


	public CacheMemory(int id, int numReplica, CacheParameter parameter, int blockSize) {
		this.id = id;
		this.parameter = parameter;
		this.regions = new HashMap<ReplicaLevel, Region>();

//		setUpRegions(numReplica, blockSize);
	}

	protected abstract void setUpRegions(int numReplica, int blockSize);

	public CacheResponse read(Block block) {
		Region region = regions.get(block.getRepLevel());
		assert region != null;

		Block result = region.read(block);
		CacheResponse response =
			new CacheResponse(parameter.getLatency(), result);

		// cache memory read log.
		logger.trace(
				String.format(
						"CM[%d] Region:%d time:%.5f read blockId:%d hit:%d",
						this.id,
						block.getRepLevel().getValue(),
						block.getAccessTime(),
						block.getId(),
						result.equals(Block.NULL) ? 0 : 1
						));

		// cache memory read statistics
		RAPoSDAStats.incrementCacheMemoryAccessCount(
				RequestType.READ, !(result.equals(Block.NULL)));

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
						"CM[%d] Region:%d time:%.5f write blockId:%d overflow:%d ofLength:%d",
						this.id,
						block.getRepLevel().getValue(),
						block.getAccessTime(),
						block.getId(),
						response.getOverflows().length == 0 ? 0 : 1,
						response.getOverflows().length));

		// TODO should be refactoring with read logging and statistics code.
		// cache memory read statistics
		RAPoSDAStats.incrementCacheMemoryAccessCount(
				RequestType.WRITE, response.getOverflows().length == 0);

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
