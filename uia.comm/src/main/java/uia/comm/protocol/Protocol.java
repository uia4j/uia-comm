package uia.comm.protocol;

/**
 * 
 * @author Kyle
 * 
 */
public interface Protocol<C> {

	/**
	 * Get alias name.
	 * 
	 * @return Alias name.
	 */
	public String getAliasName();

	/**
	 * add the handler of event.
	 * 
	 * @param handler The handler.
	 */
	public void addMessageHandler(ProtocolEventHandler<C> handler);

	/**
	 * Remove the handler of event.
	 * 
	 * @param handler The handler.
	 */
	public void remmoveMessageHandler(ProtocolEventHandler<C> handler);

	/**
	 * Create monitor to validate data based on this protocol.
	 * 
	 * @param name The monitor name.
	 * @return The monitor of protocol.
	 */
	public ProtocolMonitor<C> createMonitor(String name);
}
