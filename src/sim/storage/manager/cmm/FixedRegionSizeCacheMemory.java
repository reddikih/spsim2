package sim.storage.manager.cmm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sim.storage.CacheParameter;
import sim.storage.util.ReplicaLevel;

public class FixedRegionSizeCacheMemory extends CacheMemory {

	private static Logger logger = LoggerFactory.getLogger(FixedRegionSizeCacheMemory.class);

	public FixedRegionSizeCacheMemory(
			int id, int numReplica, CacheParameter parameter, int blockSize) {
		super(id, numReplica, parameter, blockSize);
	}

	@Override
	protected void setUpRegions(int numReplica, int blockSize) {
		// cache memory capacity(number of blocks) per region
		int maxEntries = (int)Math.floor(this.parameter.getCapacity() / blockSize / numReplica);
		for (ReplicaLevel repLevel : ReplicaLevel.values()) {
			if (numReplica <= 0) break;
			regions.put(repLevel, new Region(maxEntries));
			numReplica--;

			// log
			logger.debug(
					String.format(
							"CM[%d] fixed RepLevel:%d maxEntries:%d",
							this.id, repLevel.getValue(), maxEntries));
		}
	}

}
