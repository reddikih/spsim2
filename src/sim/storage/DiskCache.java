package sim.storage;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import sim.Block;

public class DiskCache implements Cache {

	private final int MAX_ENTRIES;
	private int diskId;

	private CacheParameter parameter;

	private HashMap<BigInteger, Block> caches;
	private TreeMap<Double, BigInteger> usedKeys;

	public DiskCache(int diskId, CacheParameter parameter, int blockSize) {
		this.diskId = diskId;
		this.parameter = parameter;
		this.MAX_ENTRIES = (int)Math.ceil((double)this.parameter.getCapacity() / blockSize);
		this.caches = new HashMap<BigInteger, Block>();
		this.usedKeys = new TreeMap<Double, BigInteger>();
	}

	@Override
	public CacheResponse read(Block block) {
		Block[] blocks = new Block[0];
		Block read = getEntry(block);
		if (read != null) {
			blocks = new Block[] {read};
		}
		return new CacheResponse(parameter.getLatency(), blocks);
	}

	@Override
	public CacheResponse write(Block block) {
		Block[] blocks = new Block[0];
		Block cachedBlock = getEntry(block);
		if (cachedBlock != null) {
		} else {
			if (caches.size() < MAX_ENTRIES) {
				addEntry(block);
			} else {
				blocks = new Block[]{replaceEntry(block)};
			}
		}
		return new CacheResponse(parameter.getLatency(), blocks);
	}

	private Block getEntry(Block block) {
		Block result = null;
		BigInteger blockId = block.getId();
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
		Map.Entry<Double, BigInteger> lruEntry = usedKeys.pollFirstEntry();
		removed = caches.remove(lruEntry.getValue());
		addEntry(block);
		return removed;
	}

	public int getDiskId() {
		return diskId;
	}

}
