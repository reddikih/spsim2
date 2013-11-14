package sim.storage.manager;

import sim.Block;
import sim.Request;
import sim.Response;
import sim.statistics.RAPoSDAStats;
import sim.storage.CacheResponse;
import sim.storage.DiskResponse;
import sim.storage.manager.cdm.MAIDCacheDiskManager;
import sim.storage.manager.cmm.MAIDCacheMemoryManager;
import sim.storage.manager.cmm.RAPoSDACacheWriteResponse;
import sim.storage.manager.ddm.MAIDDataDiskManager;
import sim.storage.util.DiskInfo;
import sim.storage.util.RequestType;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class MAIDStorageManager extends StorageManager {

    protected MAIDCacheMemoryManager cmm;
    protected MAIDCacheDiskManager cdm;
    protected MAIDDataDiskManager ddm;

    public MAIDStorageManager(
            MAIDCacheMemoryManager cmm,
            MAIDCacheDiskManager cdm,
            MAIDDataDiskManager ddm,
            int blockSize,
            int numReplica) {
        super(cmm, cdm, ddm);
        this.cmm = cmm;
        this.cdm = cdm;
        this.ddm = ddm;
        this.blockSize = blockSize;
        this.numReplica = numReplica;

        this.requestMap = new HashMap<Long, Block[]>();
    }

    @Override
    public Response read(Request request) {
        Block[] blocks = requestMap.get(request.getKey());
        if (blocks == null) throw new IllegalArgumentException();

        updateArrivalTimeOfBlocks(blocks, request.getArrvalTime());

        double respTime = Double.MIN_VALUE;

        for (Block b : blocks) {
            // block access count log
            RAPoSDAStats.incrementBlockAccessCount(RequestType.READ);

            // retrieve from cache memory
            CacheResponse cmResp = cmm.read(b);
            if (!Block.NULL.equals(cmResp.getResult())) {
                respTime =
                        respTime < cmResp.getResponseTime()
                                ? cmResp.getResponseTime() : respTime;
            } else {
                // retrieve from cache disk
                CacheResponse cdResp = cdm.read(b);
                if (!Block.NULL.equals(cdResp.getResult())) {
                    respTime =
                            respTime < cdResp.getResponseTime()
                                    ? cdResp.getResponseTime() : respTime;
                } else {
                    // read from data disk.
                    DiskResponse ddResponse = readFromDataDisk(b);
                    assert ddResponse.getResults().length == 1;
                    respTime =
                            respTime < ddResponse.getResponseTime()
                                    ? ddResponse.getResponseTime() : respTime;
                }
            }
        }
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Response write(Request request) {
        Block[] blocks = requestMap.get(request.getKey());
        if (blocks == null) {
            // new request
            blocks = divideRequest(request);
            requestMap.put(request.getKey(), blocks);
        } else {
            // update(override) request
            updateArrivalTimeOfBlocks(blocks, request.getArrvalTime());
        }

        double responseTime = Double.MIN_VALUE;
        RAPoSDACacheWriteResponse cmResponse;
        DiskResponse cdResponse;

        for (Block block : blocks) {
            // block access count log
            RAPoSDAStats.incrementBlockAccessCount(RequestType.WRITE);
            Block[] replicas = createReplicas(block);

            // write the primary block to the cache memory
            cmResponse = this.cmm.write(replicas[0]);

            // write to the cache disk
            updateArrivalTimeOfBlocks(replicas, block.getAccessTime() + cmResponse.getResponseTime());
            cdResponse = this.cdm.write(new Block[]{replicas[0]});

            // write to the data disks
            updateArrivalTimeOfBlocks(replicas, replicas[0].getAccessTime() + cdResponse.getResponseTime());
            DiskResponse ddResponse = this.ddm.write(replicas);

            responseTime = responseTime < ddResponse.getResponseTime()
                    ? ddResponse.getResponseTime() : responseTime;
        }

        return new Response(request.getKey(), responseTime);
    }

    private DiskResponse readFromDataDisk(Block block) {
        List<DiskInfo> relatedDiskInfos = ddm.getRelatedDisksInfo(block);
        DiskInfo targetDiskInfo = getADiskInfoAtRandom(relatedDiskInfos);
        return actualRead(block, targetDiskInfo);
    }

    private DiskInfo getADiskInfoAtRandom(List<DiskInfo> diskInfos) {
        Random random = new Random();
        return diskInfos.get(random.nextInt(diskInfos.size()));
    }

    private DiskResponse actualRead(Block block, DiskInfo diskInfo) {
        if (diskInfo == null)
            throw new IllegalArgumentException("diskInfo is null");
        int ownerDiskId = assignOwnerDiskId(
                block.getPrimaryDiskId(), diskInfo.getRepLevel());
        block.setOwnerDiskId(ownerDiskId);
        block.setRepLevel(diskInfo.getRepLevel());
        return ddm.read(new Block[]{block});
    }

    @Override
    public void close(double closeTime) {
        cdm.close(closeTime);
        ddm.close(closeTime);
    }
}
