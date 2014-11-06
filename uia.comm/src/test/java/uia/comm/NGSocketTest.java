package uia.comm;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Ignore;
import org.junit.Test;

import uia.comm.my.MyClientRequest;
import uia.comm.my.MyManager;
import uia.comm.protocol.ht.HTProtocol;
import uia.comm.protocol.ng.NGProtocol;

public class NGSocketTest {

	public static Logger logger = Logger.getLogger(NGSocketTest.class);

	private final NGProtocol<SocketDataController> serverProtocol;

	private final HTProtocol<SocketDataController> clientProtocol;

	private final SocketServer server;

	private final MyManager manager;

	private final MyClientRequest clientRequest;

	public NGSocketTest() throws Exception {
		PropertyConfigurator.configure("log4j.properties");

		this.manager = new MyManager();
		this.clientRequest = new MyClientRequest();

		this.serverProtocol = new NGProtocol<SocketDataController>();
		this.clientProtocol = new HTProtocol<SocketDataController>(
		        new byte[] { (byte) 0x8a },
		        new byte[] { (byte) 0xa8 });

		this.server = new SocketServer(this.serverProtocol, 5953, this.manager, "s");
		this.server.registerCallin(this.clientRequest);
		this.server.addServerListener(new SocketServerListener() {

			@Override
			public void connected(SocketDataController controller) {
				logger.info("clientName: " + controller.getName());
			}

			@Override
			public void disconnected(SocketDataController controller) {
			}

		});
	}

	public void before() throws Exception {
		this.server.start();
		Thread.sleep(1000);
	}

	public void after() throws Exception {
		this.server.stop();
	}

	@Test
	@Ignore
	public void testInOut() throws Exception {
		byte[] data = new byte[3000];
		data[0] = (byte) 0x8a;
		data[1] = 0x41;
		data[2] = 0x42;
		data[3] = 0x43;
		data[4] = 0x32;
		for (int i = 5; i < 2999; i++) {
			data[i] = (byte) (0x41 + i % 10);
		}
		data[1999] = (byte) 0xa8;

		SocketClient client = new SocketClient(this.clientProtocol, this.manager, "c1");
		client.connect("localhost", 5953);

		client.send(
		        data,
		        this.clientRequest,
		        2000);
		Thread.sleep(3000);

		// close
		client.disconnect();
		Thread.sleep(5000);
	}
}
