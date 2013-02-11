package sim.storage.manager.ddm;

import java.util.List;

import sim.Block;
import sim.storage.DiskResponse;
import sim.storage.util.DiskInfo;

public class NormalDataDiskManager implements IDataDiskManager {

	@Override
	public DiskResponse read(Block[] blocks) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DiskResponse write(Block[] blocks) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getNumberOfDataDisks() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<DiskInfo> getRelatedDisksInfo(Block block) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void close(double closeTime) {
		// TODO Auto-generated method stub

	}

}
