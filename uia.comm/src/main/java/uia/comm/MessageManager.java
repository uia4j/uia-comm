package uia.comm;

public interface MessageManager {

	/**
	 * Check if this is a CallIn command.
	 * 
	 * @param cmd Command name.
	 * @return True if it is a CallIn command.
	 */
	public boolean isCallIn(String cmd);

	/**
	 * Find name of command from data.
	 * 
	 * @param data Data.
	 * @return Command name.
	 */
	public String findCmd(byte[] data);

	/**
	 * Find transaction id from data.
	 * 
	 * @param data Data.
	 * @return Transaction id.
	 */
	public String findTx(byte[] data);
	
	/**
	 * Decode data to domain format.
	 * @param data Original data.
	 * @return Result.
	 */
	public byte[] decode(byte[] data);
	
	/**
	 * Encode domain data to byte array.
	 * @param data Domain data.
	 * @return Result.
	 */
	public byte[] encode(byte[] data);

}
