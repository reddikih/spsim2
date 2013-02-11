package sim.storage.manager.cmm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sim.storage.CacheParameter;
import sim.storage.util.ReplicaLevel;

public class VariableRegionSizeCacheMemory extends CacheMemory {

	private double bufferCoefficient;
	private static Logger logger = LoggerFactory.getLogger(VariableRegionSizeCacheMemory.class);

	public VariableRegionSizeCacheMemory(
			int id, int numReplica, CacheParameter parameter, int blockSize) {
		super(id, numReplica, parameter, blockSize);
	}

	public void setBufferCoefficient(double value) {
		this.bufferCoefficient = value;
	}

	@Override
	protected void setUpRegions(int numReplica, int blockSize) {
		// TODO Need refactoring to generalize
		int maxEntries;
		int baseEntries = (int)Math.floor(this.parameter.getCapacity() / blockSize / numReplica);
		if (numReplica == 1) {
			maxEntries = baseEntries;
			regions.put(ReplicaLevel.ZERO, new Region(maxEntries));
		} else if (numReplica == 2) {
			maxEntries = (int)Math.floor((1.0 - bufferCoefficient) * baseEntries);
			regions.put(ReplicaLevel.ZERO, new Region(maxEntries));
			maxEntries = (int)Math.floor((1 + bufferCoefficient) * baseEntries);
			regions.put(ReplicaLevel.ONE, new Region(maxEntries));
		} else if (numReplica == 3) {
			maxEntries = (int)Math.floor((1.0 - bufferCoefficient) * baseEntries);
			regions.put(ReplicaLevel.ZERO, new Region(maxEntries));
			maxEntries = baseEntries;
			regions.put(ReplicaLevel.ONE, new Region(maxEntries));
			maxEntries = (int)Math.floor((1 + bufferCoefficient) * baseEntries);
			regions.put(ReplicaLevel.TWO, new Region(maxEntries));
		} else if (numReplica == 4) {
			maxEntries = (int)Math.floor((1.0 - bufferCoefficient) * baseEntries);
			regions.put(ReplicaLevel.ZERO, new Region(maxEntries));
			maxEntries = baseEntries;
			regions.put(ReplicaLevel.ONE, new Region(maxEntries));
			regions.put(ReplicaLevel.TWO, new Region(maxEntries));
			maxEntries = (int)Math.floor((1 + bufferCoefficient) * baseEntries);
			regions.put(ReplicaLevel.THREE, new Region(maxEntries));
		} else {
			throw new IllegalArgumentException(
					"We have not generalized excepted in the case of 2, 3, or 4.");
		}

		// log
		for (ReplicaLevel repLevel : regions.keySet()) {
			Region region = regions.get(repLevel);
			logger.debug(
					String.format(
							"CM[%d] variable RepLevel:%d maxEntries:%d coefficient:%.2f",
							this.id,
							repLevel.getValue(),
							region.getMaxEntries(),
							this.bufferCoefficient));
		}
	}

}
