package uia.comm;

/**
 * 
 * @author Kyle
 * 
 */
public interface SocketServerListener {

	/**
	 * 
	 * @param controller
	 */
	public void connected(SocketDataController controller);

	/**
	 * 
	 * @param controller
	 */
	public void disconnected(SocketDataController controller);
}
