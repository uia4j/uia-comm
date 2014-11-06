package uia.comm;

/**
 * 
 * @author Kyle
 * 
 */
public interface MessageCallOut {

	/**
	 * Get transaction id.
	 * 
	 * @return
	 */
	public String getTxId();

	/**
	 * Executed when receive message from remote.
	 * 
	 * @param reply reply data from remote.
	 */
	public void execute(byte[] reply);

	/**
	 * Executed if there is no answer from remote.
	 */
	public void timeout();

}
