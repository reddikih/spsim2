package sim.storage.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import sim.Block;
import sim.Request;
import sim.Response;
import sim.statistics.RAPoSDAStats;
import sim.storage.CacheResponse;
import sim.storage.DiskResponse;
import sim.storage.manager.cdm.RAPoSDACacheDiskManager;
import sim.storage.manager.cmm.RAPoSDACacheMemoryManager;
import sim.storage.manager.cmm.RAPoSDACacheWriteResponse;
import sim.storage.manager.ddm.RAPoSDADataDiskManager;
import sim.storage.util.DiskInfo;
import sim.storage.util.DiskState;
import sim.storage.util.RequestType;

public class RAPoSDAStorageManager extends StorageManager {

	protected RAPoSDACacheMemoryManager cmm;
	protected RAPoSDACacheDiskManager cdm;
	protected RAPoSDADataDiskManager ddm;

	public RAPoSDAStorageManager(
			RAPoSDACacheMemoryManager cmm,
			RAPoSDACacheDiskManager cdm,
			RAPoSDADataDiskManager ddm,
			int blockSize,
			int numReplica) {
		super(ddm);

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
					DiskResponse ddResp = readFromDataDisk(b);
					assert ddResp.getResults().length == 1;
					respTime =
						respTime < ddResp.getResponseTime()
						? ddResp.getResponseTime() : respTime;

					// write to cache disk asynchronously
					// after read from data disk.
					writeToCacheDisk(
							ddResp.getResults(),
							ddResp.getResponseTime() + b.getAccessTime());
				}
			}
		}
		return new Response(request.getKey(), respTime);
	}

	private DiskResponse readFromDataDisk(Block block) {

		List<DiskInfo> relatedDiskInfos = ddm.getRelatedDisksInfo(block);
		List<DiskInfo> activeDiskInfos = extractActiveDisks(relatedDiskInfos);

		// case 1. one of n disks is spinning.
		if (activeDiskInfos.size() == 1)
			return actualRead(block, activeDiskInfos.get(0));

		// case 2. some of n disks are spinning.
		if (activeDiskInfos.size() > 1
				&& activeDiskInfos.size() <= relatedDiskInfos.size()) {
			DiskInfo diskState = cmm.getMaxBufferDisk(activeDiskInfos);
			return actualRead(block, diskState);
		}

		// case 3. all of n disks are stopping.
		assert activeDiskInfos.size() == 0;
		DiskInfo diskState =
				ddm.getLongestStandbyDiskInfo(
						relatedDiskInfos,
						block.getAccessTime());
		return actualRead(block, diskState);
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

	private List<DiskInfo> extractActiveDisks(List<DiskInfo> diskInfos) {
		List<DiskInfo> result = new ArrayList<DiskInfo>();
		for (DiskInfo dInfo : diskInfos) {
			if (DiskState.ACTIVE.equals(dInfo.getDiskState())
					|| DiskState.IDLE.equals(dInfo.getDiskState()))
				result.add(dInfo);
		}
		return result;
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
			for (Block b : replicas) {
				RAPoSDACacheWriteResponse response = cmm.write(b);
				if (response.getOverflows().length == 0) {
					respTime =
						response.getResponseTime() > respTime
						? response.getResponseTime() : respTime;
				} else { // cache overflow

					// overflow statistics
					RAPoSDAStats.incrementOverflowCount();

					double arrivalTime =
						request.getArrvalTime() + response.getResponseTime();

					// write to data disk
					DiskResponse ddResp =
							writeToDataDisk(response, arrivalTime);

					// write to cache disk asynchronously
					// after data disk write.
					writeToCacheDisk(
							ddResp.getResults(),
							ddResp.getResponseTime() + arrivalTime);

					// delete blocks on the cache already written in disks.
					double cmDeletedTime = deleteBlocksOnCache(
							ddResp.getResults(),
							ddResp.getResponseTime() + arrivalTime);

					// return least response time;
					double tempResp =
						ddResp.getResponseTime() + cmDeletedTime;

					respTime = respTime < tempResp ? tempResp : respTime;
				}
			}
		}
		return new Response(request.getKey(), respTime);
	}

	private double deleteBlocksOnCache(Block[] blocks, double arrivalTime) {
		updateArrivalTimeOfBlocks(blocks, arrivalTime);
		double response = Double.MIN_VALUE;
		for (Block block : blocks) {
			CacheResponse cmResp = cmm.remove(block);
			response =
				response < cmResp.getResponseTime()
				? cmResp.getResponseTime() : response;
		}
		return response;
	}

	private DiskResponse writeToDataDisk(
			RAPoSDACacheWriteResponse cmResp, double arrivalTime) {
		int maxBufferDiskId = cmResp.getMaxBufferDiskId();
		if(!ddm.isSpinning(maxBufferDiskId, arrivalTime))
			ddm.spinUp(maxBufferDiskId, arrivalTime);
		Block[] blocks = cmResp.getOverflows();
		updateArrivalTimeOfBlocks(blocks, arrivalTime);
		return ddm.write(blocks);
	}

	private DiskResponse writeToCacheDisk(Block[] blocks, double arrivalTime) {
		updateArrivalTimeOfBlocks(blocks, arrivalTime);
		return cdm.write(blocks);
	}
	
	private List<Integer> getSpinningDiskIds(double timestamp) {
		List<DiskState> states = new ArrayList<DiskState>();
		states.add(DiskState.ACTIVE);
		states.add(DiskState.IDLE);
		return ddm.getSpecificStateDiskIds(timestamp, states);
	}
	
	private List<Integer> getStandbyDiskIds(double timestamp) {
		List<DiskState> states = new ArrayList<DiskState>();
		states.add(DiskState.STANDBY);
		return ddm.getSpecificStateDiskIds(timestamp, states);
	}

	@Override
	public void close(double closeTime) {
		cdm.close(closeTime);
		ddm.close(closeTime);
	}

}
