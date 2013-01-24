package sim;

public class Parameter {

	///--- System parameters. ---///
	public static final int NUMBER_OF_CACHE_MEMORIES = 3;
	public static final int NUMBER_OF_CACHE_DISKS = 1;
	public static final int NUBER_OF_DISKS_PER_CACHE_GROUP = 2;

	/** Spindown threshold time represents by second. */
	public static final double SPINDOWN_THRESHOLD = 10.0;

//	public static final int BLOCK_SIZE = 4 * 1024;
	public static final int BLOCK_SIZE = 1;

	public static final int NUMBER_OF_REPLICA = 2;


	///--- Cache memory parameters. ---///
	/** Capacity threshold ratio of cache memory. (0.0 - 1.0) */
	public static final double CACHE_MEMORY_THRESHOLD = 1.0;

	/** Cache memory size. 4GB by default. */
//	public static final long CACHE_MEMORY_SIZE = 4 * 1024 * 1024 * 1024;
	public static final long CACHE_MEMORY_SIZE = 0;

	/** Memory access latency represents by second. */
	public static final double CACHE_MEMORY_LATENCY = 0.00001;


	///--- HDD parameters. ---///
	/** HDD size. 2TB by spec. */
	public static final long HDD_SIZE = 2L * 1024 * 1024 * 1024 * 1024;

	public static final int HDD_NUMBER_OF_PLATTER = 5;

	public static final int HDD_RPM = 7200;

	/** HDD disk cache size. 32MB by spec. */
	public static final long HDD_CACHE_SIZE = 32 * 1024 * 1024;

	public static final long HDD_TRANSFER_RATE = 134 * 1024 * 1024;

	public static final int HDD_SECTORS_PER_TRACK = 63;

	public static final double HDD_FULL_STROKE_SEEK_TIME = 0.0087;

	public static final double HDD_HEAD_SWITCH_OVERHEAD = 0.00084;

	public static final double HDD_COMMAND_OVERHEAD = 0.0005;

	public static final double HDD_ACTIVE_POWER = 11.1;

	public static final double HDD_IDLE_POWER = 7.5;

	public static final double HDD_STANDBY_POWER = 0.8;

	/** HDD spindown energy represents by joule(w/s) */
	public static final double HDD_SPINDOWN_ENERGY = 35.0;

	public static final double HDD_SPINUP_ENERGY = 450.0;

	public static final double HDD_SPINDOWN_TIME = 0.7;

	public static final double HDD_SPINUP_TIME = 15;
}
