package sim.storage.manager.cmm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import sim.Block;

public class Chunk {
	
	private int diskId;
	private int replicaLevel;
	private List<Block> blocks;
	
	public Chunk(int diskId, int replicaLevel) {
		this.diskId = diskId;
		this.replicaLevel = replicaLevel;
		this.blocks = new ArrayList<Block>();
	}
	
	public void addBlock(Block block) {
		if (block == null || block.equals(Block.NULL))
			throw new NullPointerException("Block is null!!");
		this.blocks.add(block);
	}
	
	public void addBlocks(Collection<Block> blocks) {
		if (blocks == null)
			throw new NullPointerException("Blocks is null!!");
		this.blocks.addAll(blocks);
	}
	
	public int getDiskId() {
		return this.diskId;
	}
	
	public int getReplicaLevel() {
		return this.replicaLevel;
	}
	
	public List<Block> getBlocks() {
		return this.blocks;
	}

}
