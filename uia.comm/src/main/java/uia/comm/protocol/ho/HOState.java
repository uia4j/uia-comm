package uia.comm.protocol.ho;

public interface HOState<T> {

	/**
	 * handle one byte from data channel.
	 * 
	 * @param monitor one which monitors data channel.
	 * @param one data.
	 */
	public void accept(HOProtocolMonitor<T> monitor, byte one);

	/**
	 * handle when no data in data channel
	 * 
	 * @param monitor one which monitors data channel.
	 */
	public void end(HOProtocolMonitor<T> monitor);
}
