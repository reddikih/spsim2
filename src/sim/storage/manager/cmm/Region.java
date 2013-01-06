package sim.storage.manager.cmm;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import sim.Block;

public class Region {

	private final int maxEntries;

	/**
	 * Cached blocks in this memory region.
	 *
	 * key: block id
	 * value: Block object
	 */
	private HashMap<BigInteger, Block> blocks;

	/**
	 * Buffered block length(queue size) per disk.
	 *
	 * key: disk id
	 * value: buffer length of corresponding disk id.
	 */
	private HashMap<Integer, Integer> bufferLengthCounter;

	private final static Block[] emptyBlocks =
		new HashMap<BigInteger, Block>().values().toArray(new Block[0]);


	public Region(int maxEntries) {
		this.maxEntries = maxEntries;
		this.blocks = new HashMap<BigInteger, Block>();
		this.bufferLengthCounter = new HashMap<Integer, Integer>();
	}

	public Block read(Block block) {
		Block result = Block.NULL;
		if (blocks.containsKey(block.getId()))
			result = blocks.get(block.getId());
		return result;
	}

	public Block write(Block block) {
		Block result = Block.NULL;
		if (blocks.containsKey(block.getId())) {
			blocks.put(block.getId(), block);
			result = block;
		} else {
			if (blocks.size() < maxEntries ) {
				blocks.put(block.getId(), block);
				result = block;
				incrementBufferCounter(block.getOwnerDiskId());
			} else {
				// overflow
				blocks.put(block.getId(), block);
				incrementBufferCounter(block.getOwnerDiskId());
			}
		}
		return result;
	}

	public Block remove(Block block) {
		Block result = Block.NULL;
		if (block == null) return result;
		result = blocks.remove(block.getId());
		decrementBufferCounter(block.getOwnerDiskId());

		return result;
	}

	public Block[] getBlocks() {
		return blocks.values().toArray(new Block[0]);
	}

	public Block[] getEmptyBlocks() {
		return emptyBlocks;
	}

	public int getMaxBufferLenghtDiskId() {
		int maxLength = Integer.MIN_VALUE;
		int diskId = Integer.MIN_VALUE;
		for (Map.Entry<Integer, Integer> entry : bufferLengthCounter.entrySet()) {
			if (maxLength < entry.getValue()) {
				maxLength = entry.getValue();
				diskId = entry.getKey();
			}
		}
		return diskId;
	}

	private void incrementBufferCounter(int diskId) {
		if (bufferLengthCounter.containsKey(diskId)) {
			int count = bufferLengthCounter.get(diskId) + 1;
			bufferLengthCounter.put(diskId, count);
		} else {
			bufferLengthCounter.put(diskId, 1);
		}
	}

	private void decrementBufferCounter(int diskId) {
		if (bufferLengthCounter.containsKey(diskId)) {
			int count = bufferLengthCounter.get(diskId) - 1;
			if (count <= 0) {
				// if count less than equal zero, remove from counter map.
				bufferLengthCounter.remove(diskId);
			} else {
				bufferLengthCounter.put(diskId, count);
			}
		}
	}

	public int getBufferLenght() {
		return blocks.size();
	}

}
