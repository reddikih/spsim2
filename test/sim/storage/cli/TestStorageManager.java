package sim.storage.cli;

import sim.Block;
import sim.storage.manager.RAPoSDAStorageManager;
import sim.storage.manager.buffer.RAPoSDABufferManagerFactory;
import sim.storage.manager.cdm.RAPoSDACacheDiskManager;
import sim.storage.manager.cmm.RAPoSDACacheMemoryManager;
import sim.storage.manager.ddm.RAPoSDADataDiskManager;

public class TestStorageManager extends RAPoSDAStorageManager {

	public TestStorageManager(RAPoSDACacheMemoryManager cmm,
			RAPoSDACacheDiskManager cdm, RAPoSDADataDiskManager ddm,
			RAPoSDABufferManagerFactory factory,
			int blockSize, int numReplica) {
		super(cmm, cdm, ddm, factory, blockSize, numReplica);
	}
	
	public int requestMapSize() {
		return this.requestMap.size();
	}
	
	@Override
	public void register(long requestId, int size) {
		Block[] blocks = null;
		int numBlocks = (int)Math.ceil(size / blockSize);
		assert numBlocks > 0;
		blocks = new Block[numBlocks];
		for (int i=0; i < numBlocks; i++) {
			long blockId = nextBlockId();
			blocks[i] = new Block(
					blockId,
					0.0,
					0);
		}
		requestMap.put(requestId, blocks);
	}

}
