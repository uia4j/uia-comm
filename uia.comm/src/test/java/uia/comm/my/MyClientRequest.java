package uia.comm.my;

import org.apache.log4j.Logger;

import uia.comm.MessageCallIn;
import uia.comm.MessageCallOut;
import uia.comm.SocketDataController;

public class MyClientRequest implements MessageCallOut, MessageCallIn<SocketDataController> {

	public static Logger logger = Logger.getLogger(MyClientRequest.class);

	@Override
	public String getCmdName() {
		return "ABC";
	}

	@Override
	public String getTxId() {
		return "2";
	}

	@Override
	public void execute(byte[] reply) {
		logger.info("client reply: " + new String(reply));
	}

	@Override
	public void timeout() {
		logger.info("timeout");
	}

	@Override
	public void execute(byte[] request, SocketDataController controller) {
		try {
			logger.info(controller.getName() + " callin> data len: " + request.length);
			logger.info(controller.getName() + " callin> data: " + new String(request));
			controller.send(new byte[] { (byte) 0x8a, 0x44, 0x45, 0x46, 0x32, 0x31, (byte) 0xa8 }, 1);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
