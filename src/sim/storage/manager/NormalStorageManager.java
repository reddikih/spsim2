package sim.storage.manager;

import sim.Block;
import sim.Request;
import sim.Response;
import sim.statistics.RAPoSDAStats;
import sim.storage.DiskResponse;
import sim.storage.manager.cmm.NormalCacheMemoryManager;
import sim.storage.manager.ddm.NormalDataDiskManager;
import sim.storage.util.RequestType;

import java.util.HashMap;

public class NormalStorageManager extends StorageManager {

    protected NormalCacheMemoryManager cmm;
    protected NormalDataDiskManager ddm;

    public NormalStorageManager(
            NormalCacheMemoryManager cmm,
            NormalDataDiskManager ddm,
            int blockSize,
            int numReplica) {
        super(cmm, null, ddm);

        this.cmm = cmm;
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
            // TODO change to normal statistics.
            // block access count log
            RAPoSDAStats.incrementBlockAccessCount(RequestType.READ);

            DiskResponse dResp = ddm.read(new Block[]{b});
            respTime = respTime >= dResp.getResponseTime()
                    ? respTime : dResp.getResponseTime();
        }
        return new Response(request.getKey(), respTime);
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

        double respTime = Double.MIN_VALUE;

        for (Block block : blocks) {
            // block access count log
            RAPoSDAStats.incrementBlockAccessCount(RequestType.WRITE);

            Block[] replicas = createReplicas(block);

            DiskResponse dResp = ddm.write(replicas);
            respTime = respTime >= dResp.getResponseTime()
                    ? respTime : dResp.getResponseTime();
        }
        return new Response(request.getKey(), respTime);
    }

    @Override
    public void close(double closeTime) {
        ddm.close(closeTime);
    }
}
