package sim.storage.manager.buffer;

import sim.Request;
import sim.Response;

public interface IBufferManager {
	
	public Response write(Request request);

}
