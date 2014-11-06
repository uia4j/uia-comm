package uia.comm.protocol;

/**
 * 
 * @author Kyle
 * 
 * @param <C>
 */
public interface ProtocolMonitor<C> {

	/**
	 * Get protocol which creates this monitor.
	 * 
	 * @return The protocol.
	 */
	public Protocol<C> getProtocol();

	/**
	 * Get the name.
	 * 
	 * @return The name.
	 */
	public String getName();

	/**
	 * Read a byte from input source.
	 * 
	 * @param one One byte.
	 */
	public void read(byte one);

	/**
	 * call when no data in data channel.
	 */
	public void readEnd();

	/**
	 * reset monitor to idle state.
	 */
	public void reset();

	/**
	 * Get the controller of the monitor.
	 * 
	 * @return The controller.
	 */
	public C getController();

	/**
	 * Set the controller of ths monitor.
	 * 
	 * @param controller The controller.
	 */
	public void setController(C controller);
}
