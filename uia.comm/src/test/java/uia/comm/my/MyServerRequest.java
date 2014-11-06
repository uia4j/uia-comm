package uia.comm.my;

import org.apache.log4j.Logger;

import uia.comm.MessageCallIn;
import uia.comm.MessageCallOut;
import uia.comm.SocketDataController;

public class MyServerRequest implements MessageCallIn<SocketDataController>, MessageCallOut {

	public static Logger logger = Logger.getLogger(MyServerRequest.class);

	@Override
	public String getCmdName() {
		return "ABC";
	}

	@Override
	public String getTxId() {
		return "1";
	}

	@Override
	public void execute(byte[] request, SocketDataController controller) {
		try {
			logger.info(controller.getName() + " callin: " + new String(request));
			controller.send(new byte[] { (byte) 0x8a, 0x44, 0x45, 0x46, 0x31, 0x32, (byte) 0xa8 }, 1);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	@Override
	public void execute(byte[] reply) {
		logger.info("server reply: " + new String(reply));
	}

	@Override
	public void timeout() {
		logger.info("svr request timeout");
	}

}
