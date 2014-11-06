package uia.comm;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Ignore;
import org.junit.Test;

import uia.comm.my.MyClientRequest;
import uia.comm.my.MyManager;
import uia.comm.my.MyServerRequest;
import uia.comm.protocol.ht.HTProtocol;

public class SocketTest {

	public static Logger logger = Logger.getLogger(SocketTest.class);

	private final HTProtocol<SocketDataController> serverProtocol;

	private final HTProtocol<SocketDataController> clientProtocol;

	private final SocketServer server;

	private final MyManager manager;

	private final MyServerRequest serverRequest;

	private final MyClientRequest clientRequest;

	private String clientId;

	public SocketTest() throws Exception {
		PropertyConfigurator.configure("log4j.properties");

		this.manager = new MyManager();
		this.serverRequest = new MyServerRequest();
		this.clientRequest = new MyClientRequest();

		this.serverProtocol = new HTProtocol<SocketDataController>(
		        new byte[] { (byte) 0x8a },
		        new byte[] { (byte) 0xa8 });

		this.clientProtocol = new HTProtocol<SocketDataController>(
		        new byte[] { (byte) 0x8a },
		        new byte[] { (byte) 0xa8 });

		this.server = new SocketServer(this.serverProtocol, 5953, this.manager, "s");
		this.server.registerCallin(this.clientRequest);
		this.server.addServerListener(new SocketServerListener() {

			@Override
			public void connected(SocketDataController controller) {
				logger.info("clientName: " + controller.getName());
				SocketTest.this.clientId = controller.getName();
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
	public void testPolling() throws Exception {

		SocketClient client = new SocketClient(this.clientProtocol, this.manager, "c2");
		client.connect("localhost", 5953);
		Thread.sleep(10000);
		client.disconnect();
		Thread.sleep(10000);
	}

	@Test
	@Ignore
	public void testSend() throws Exception {

		SocketClient client = new SocketClient(this.clientProtocol, this.manager, "c2", 1234);
		client.connect("localhost", 5953);
		Thread.sleep(5000);

		this.server.disconnect("/127.0.0.1:1234");
		Thread.sleep(10000);
		System.out.println(client.send(new byte[] { 0x01, 0x02, 0x03 }));
		System.out.println(client.send(new byte[] { 0x01, 0x02, 0x03 }));
		client.disconnect();
	}

	@Test
	@Ignore
	public void testClientPort() throws Exception {

		SocketClient client = new SocketClient(this.clientProtocol, this.manager, "c2", 1234);
		client.connect("localhost", 5953);
		Thread.sleep(5000);
		client.disconnect();
	}

	@Test
	@Ignore
	public void testInOut() throws Exception {
		SocketClient client = new SocketClient(this.clientProtocol, this.manager, "c1");
		client.registerCallin(this.serverRequest);
		client.connect("localhost", 5953);

		client.send(
		        new byte[] { (byte) 0x8a, 0x41, 0x42, 0x43, 0x32, (byte) 0xa8 },
		        this.clientRequest,
		        1000);
		Thread.sleep(2000);

		this.server.send(
		        this.clientId,
		        new byte[] { (byte) 0x8a, 0x41, 0x42, 0x43, 0x31, (byte) 0xa8 },
		        this.serverRequest,
		        1000);
		Thread.sleep(2000);

		// close
		client.disconnect();
		Thread.sleep(5000);
	}
}
