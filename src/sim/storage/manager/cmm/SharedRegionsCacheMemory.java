package sim.storage.manager.cmm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sim.storage.CacheParameter;
import sim.storage.util.ReplicaLevel;

public class SharedRegionsCacheMemory extends CacheMemory {
	
	private static Logger logger = LoggerFactory.getLogger(SharedRegionsCacheMemory.class);

	public SharedRegionsCacheMemory(
			int id,
			int numReplica,
			CacheParameter parameter,
			int blockSize) {
		super(id, numReplica, parameter, blockSize);
	}

	@Override
	protected void setUpRegions(int numReplica, int blockSize) {
		int maxEntries = (int)Math.floor(this.parameter.getCapacity() / blockSize);
		Region region = new Region(maxEntries);
		for (ReplicaLevel repLevel : ReplicaLevel.values()) {
			if (numReplica <= 0) break;
			regions.put(repLevel, region);
			numReplica--;

			// log
			logger.debug(
					String.format(
							"CM[%d] Shared RepLevel:%d maxEntries:%d",
							this.id, repLevel.getValue(), maxEntries));
		}
	}

}
