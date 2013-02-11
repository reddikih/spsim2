package sim.storage.manager;

import java.util.HashMap;

import sim.Block;
import sim.Request;
import sim.Response;
import sim.storage.manager.cmm.NormalCacheMemoryManager;
import sim.storage.manager.ddm.NormalDataDiskManager;

public class NormalStorageManager extends StorageManager {

	protected NormalCacheMemoryManager cmm;
	protected NormalDataDiskManager ddm;

	public NormalStorageManager(
			NormalCacheMemoryManager cmm,
			NormalDataDiskManager ddm,
			int blockSize,
			int numReplica) {
		super(ddm);

		this.cmm = cmm;
		this.ddm = ddm;
		this.blockSize = blockSize;
		this.numReplica = numReplica;

		this.requestMap = new HashMap<Long, Block[]>();
	}

	@Override
	public Response read(Request request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response write(Request request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void close(double closeTime) {
		// TODO Auto-generated method stub

	}
}
