package sim.storage.manager.buffer;

public class BufferMonitor {
	
	private int writtenBlockCount;
	private double lastBufferOverflowTimestamp;
	
	public void addWriteBlockCount(int n) {
		assert n >= 0;
		this.writtenBlockCount += n;
	}
	
	public int getWriteBlockCount() {
		return this.writtenBlockCount;
	}
	
	public double getLastBufferOverflowTimestamp() {
		return this.lastBufferOverflowTimestamp;
	}
	
	public double getMeanArrivalRateOfWriteAccesses(double timestamp) {
		double betweenBOF = timestamp - this.lastBufferOverflowTimestamp;
		if (betweenBOF < 0.0)  {
			String times = 
					String.format(
							" lastBufferOFTime:%,.4f arrivalTime:%,.4f",
							this.lastBufferOverflowTimestamp, timestamp);
			throw new IllegalArgumentException(
					"buffer overflow timestamp should never less than the last one." +
					times);
		}
		
		betweenBOF = betweenBOF == 0.0 ? 1 : betweenBOF;
		
		double lambda = this.writtenBlockCount / betweenBOF;
		
		this.lastBufferOverflowTimestamp = timestamp;
		this.writtenBlockCount = 0;
		
		return lambda;
	}

}
