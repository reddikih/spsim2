package sim.storage;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import sim.Block;

public class DiskCache implements Cache {

	private final int MAX_ENTRIES;
	private int diskId;

	private CacheParameter parameter;

	private HashMap<Long, Block> caches;
	private TreeMap<Double, Long> usedKeys;

	public DiskCache(int diskId, CacheParameter parameter, int blockSize) {
		this.diskId = diskId;
		this.parameter = parameter;
		this.MAX_ENTRIES = (int)Math.floor((double)this.parameter.getCapacity() / blockSize);
		this.caches = new HashMap<Long, Block>();
		this.usedKeys = new TreeMap<Double, Long>();
	}

	@Override
	public CacheResponse read(Block block) {
		return new CacheResponse(parameter.getLatency(), getEntry(block));
	}

	@Override
	public CacheResponse write(Block block) {
		Block result = getEntry(block);
		if (result == Block.NULL) {
			if (caches.size() < MAX_ENTRIES) {
				addEntry(block);
				result = block;
			} else {
				result = replaceEntry(block);
			}
		}
		return new CacheResponse(parameter.getLatency(), result);
	}

	private Block getEntry(Block block) {
		Block result = Block.NULL;
		long blockId = block.getId();
		if (caches.containsKey(blockId)) {
			result = caches.get(blockId);
			usedKeys.remove(result.getAccessTime());
			usedKeys.put(block.getAccessTime(), blockId);
			result.setAccessTime(block.getAccessTime());
		}
		return result;
	}

	private void addEntry(Block block) {
		caches.put(block.getId(), block);
		usedKeys.put(block.getAccessTime(), block.getId());
	}

	private Block replaceEntry(Block block) {
		Block removed = null;
		Map.Entry<Double, Long> lruEntry = usedKeys.pollFirstEntry();
		removed = caches.remove(lruEntry.getValue());
		addEntry(block);
		return removed;
	}

	public int getDiskId() {
		return diskId;
	}

}
