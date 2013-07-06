package sim;

public class Parameter {
	
	///--- Runtime parameter ---///
	public static boolean DEBUG_FLAG = true;

	///--- System parameters. ---///
	public static int NUMBER_OF_CACHE_MEMORIES = 3;
	public static int NUMBER_OF_CACHE_DISKS = 3;
	public static int NUMBER_OF_DISKS_PER_CACHE_GROUP = 2;

	/** Spindown threshold time represents by second. */
	public static double SPINDOWN_THRESHOLD = 10.0;

//	public static int BLOCK_SIZE = 8 * 1024;
	public static int BLOCK_SIZE = 1;

	public static int NUMBER_OF_REPLICA = 3;

//	public static String WORKLOAD_FILE_PATH = "config/workload/workload.5h.rr3.lam25.the12.ds32GB";
	public static String WORKLOAD_FILE_PATH = "config/workload/workload";

	///--- Factory names of storage manager ---///
	public static String STORAGE_MANAGER_FACTORY = "sim.storage.manager.RAPoSDAStorageManagerFactory";
//	public static String STORAGE_MANAGER_FACTORY = "sim.storage.manager.NormalStorageManagerFactory";

	///--- Factory names about cache memory ---///
	public static String CACHE_MEMORY_ASSIGNOR = "cs";
//	public static String CACHE_MEMORY_ASSIGNOR = "dga";
//	public static String CACHE_MEMORY_ASSIGNOR = "simple";

	public static String CACHE_MEMORY_FACTORY = "sim.storage.manager.cmm.FixedRegionSizeCacheMemoryFactory";
//	public static String CACHE_MEMORY_FACTORY = "sim.storage.manager.cmm.VariableRegionSizeCacheMemoryFactory";
//	public static String CACHE_MEMORY_FACTORY = "sim.storage.manager.cmm.SharedRegionsCacheMemoryFactory";

	public static double CACHE_MEMORY_BUFFER_COEFFICIENT = 0.2;

	
	public static String BUFFER_MANAGER_FACTORY = "sim.storage.manager.buffer.RAPoSDABufferManagerFactory";
//	public static String BUFFER_MANAGER_FACTORY = "sim.storage.manager.buffer.FlushToAllSpinningDiskBufferManagerFactory";
//	public static String BUFFER_MANAGER_FACTORY = "sim.storage.manager.buffer.EnergyEfficientBufferManagerFactory";

	///--- Cache memory parameters. ---///
	/** Capacity threshold ratio of cache memory. (0.0 - 1.0) */
	public static double CACHE_MEMORY_THRESHOLD = 1.0;

	/** Cache memory size. 4GB by default. */
//	public static long CACHE_MEMORY_SIZE = 8L * 1024 * 1024 * 1024;
	public static long CACHE_MEMORY_SIZE = 6;

	/** Memory access latency represents by second. */
	public static double CACHE_MEMORY_LATENCY = 0.00001;


	///--- HDD parameters. ---///
	/** HDD size. 2TB by spec. */
	public static long HDD_SIZE = 2L * 1024 * 1024 * 1024 * 1024;

	public static int HDD_NUMBER_OF_PLATTER = 5;

	public static int HDD_RPM = 7200;

	/** HDD disk cache size. 32MB by spec. */
	public static long HDD_CACHE_SIZE = 32L * 1024 * 1024;

	public static long HDD_TRANSFER_RATE = 134L * 1024 * 1024;

//	public static int HDD_SECTORS_PER_TRACK = 63;
	public static int HDD_SECTORS_PER_TRACK = 573;

	public static double HDD_FULL_STROKE_SEEK_TIME = 0.0087;

	public static double HDD_HEAD_SWITCH_OVERHEAD = 0.00084;

	public static double HDD_COMMAND_OVERHEAD = 0.0005;

	public static double HDD_ACTIVE_POWER = 11.1;

	public static double HDD_IDLE_POWER = 7.5;

	public static double HDD_STANDBY_POWER = 0.8;

	/** HDD spindown energy represents by joule(w/s) */
	public static double HDD_SPINDOWN_ENERGY = 35.0;

	public static double HDD_SPINUP_ENERGY = 450.0;

	public static double HDD_SPINDOWN_TIME = 0.7;

	public static double HDD_SPINUP_TIME = 15;
}
