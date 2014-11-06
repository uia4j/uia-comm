package uia.comm;

/**
 * 
 * @author Kyle
 * 
 * @param <T>
 */
public interface MessageCallIn<T> {

	/**
	 * Get command name.
	 * 
	 * @return Command name.
	 */
	public String getCmdName();

	/**
	 * Executed when receive message from remote.
	 * 
	 * @param request The request data from remote.
	 * @param controller The controller connects to remote.
	 */
	public void execute(byte[] request, T controller);

}
