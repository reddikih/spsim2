package sim.storage.manager.ddm;

import java.util.List;

import sim.Block;
import sim.storage.DiskResponse;
import sim.storage.util.DiskInfo;

public interface IDataDiskManager {

	public DiskResponse read(Block[] blocks);

	public DiskResponse write(Block[] blocks);

	public int getNumberOfDataDisks();

	public List<DiskInfo> getRelatedDisksInfo(Block block);

	public void close(double closeTime);
}
