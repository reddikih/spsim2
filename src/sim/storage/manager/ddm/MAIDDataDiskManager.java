package sim.storage.manager.ddm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sim.Block;
import sim.storage.DiskResponse;
import sim.storage.util.DiskInfo;
import sim.storage.util.ReplicaLevel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class MAIDDataDiskManager implements IDataDiskManager {

    private final int numberOfDataDisks;
    private final int numberOfReplica;

    private HashMap<Integer, DataDisk> dataDiskMap;

    private Logger spinupLogger = LoggerFactory.getLogger("SPIN_UP_INFO_TRACE");

    public MAIDDataDiskManager(
            int numberOfDataDisks,
            int numberOfReplica,
            HashMap<Integer, DataDisk> dataDiskMap) {

        if (numberOfDataDisks != dataDiskMap.size())
            throw new IllegalArgumentException("number of data disks is invalid.");

        this.numberOfDataDisks = numberOfDataDisks;
        this.numberOfReplica = numberOfReplica;
        this.dataDiskMap = dataDiskMap;
    }

    @Override
    public DiskResponse read(Block[] blocks) {
        double arrivalTime = blocks[0].getAccessTime();
        double respTime = Double.MIN_VALUE;
        for (Block block : blocks) {
            int targetDiskId = block.getOwnerDiskId();
            double latency = 0.0;
            if (!isSpinning(targetDiskId, arrivalTime)) {
                latency = spinUp(targetDiskId, arrivalTime);
            }
            block.setAccessTime(arrivalTime + latency);

            DataDisk dd = dataDiskMap.get(targetDiskId);
            assert dd != null;
            // TODO this can be optimized for sequential access.
            double ddResp = dd.read(new Block[]{block});
            respTime = respTime < ddResp ? ddResp : respTime;
        }
        return new DiskResponse(respTime, blocks);
    }

    @Override
    public DiskResponse write(Block[] blocks) {
        List<Block> writtenBlocks = new ArrayList<Block>();
        double arrivalTime = blocks[0].getAccessTime();
        double respTime = Double.MIN_VALUE;
        for (Block block : blocks) {
            int targetDiskId = block.getOwnerDiskId();
            double latency = 0.0;
            if (!isSpinning(targetDiskId, arrivalTime)) {
                latency = spinUp(targetDiskId, arrivalTime);
            }
            block.setAccessTime(arrivalTime + latency);

            DataDisk dd = dataDiskMap.get(targetDiskId);
            assert dd != null;
            double tempResp = dd.write(new Block[]{block});
            respTime = respTime < tempResp ? tempResp : respTime;
            writtenBlocks.add(block);
        }
        return new DiskResponse(respTime, writtenBlocks.toArray(new Block[0]));
    }

    public boolean isSpinning(int diskId, double accessTime) {
        DataDisk dd = dataDiskMap.get(diskId);
        assert dd != null;
        return dd.isSpinning(accessTime);
    }

    public double spinUp(int diskId, double accessTime) {
        DataDisk dd = dataDiskMap.get(diskId);
        assert dd != null;

        // spin up log
        spinupLogger.trace(
                String.format("DISKID:%d ACCESSTIME:%,.4f", diskId, accessTime));

        return dd.spinUp(accessTime);
    }

    @Override
    public int getNumberOfDataDisks() {
        return numberOfDataDisks;
    }

    @Override
    public List<DiskInfo> getRelatedDisksInfo(Block block) {
        List<DiskInfo> diskInfos = new ArrayList<DiskInfo>();

        int i = 0;
        for (ReplicaLevel repLevel : ReplicaLevel.values()) {
            if (numberOfReplica <= i) break;
            int diskId = (block.getPrimaryDiskId() + i) % numberOfDataDisks;
            DataDisk dd = dataDiskMap.get(diskId);
            assert dd != null;
            DiskInfo info = new DiskInfo(
                    diskId,
                    dd.getState(block.getAccessTime()),
                    repLevel);
            diskInfos.add(info);
            i++;
        }
        return diskInfos;
    }

    @Override
    public void close(double closeTime) {
        Collection<DataDisk> dds = dataDiskMap.values();
        for (DataDisk dd : dds) {
            dd.close(closeTime);
        }
    }
}
