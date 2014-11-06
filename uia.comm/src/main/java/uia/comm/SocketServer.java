package uia.comm;

import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import uia.comm.protocol.Protocol;
import uia.comm.protocol.ProtocolEventArgs;
import uia.comm.protocol.ProtocolEventHandler;
import uia.comm.protocol.ProtocolMonitor;
import uia.utils.ByteUtils;

/**
 * The socket server
 * 
 * @author Kyle K. Lin
 */
public class SocketServer implements ProtocolEventHandler<SocketDataController> {

	private final static Logger logger = Logger.getLogger(SocketServer.class);

	private final Protocol<SocketDataController> protocol;

	private final MessageManager manager;

	private final HashMap<String, MessageCallIn<SocketDataController>> callIns;

	private final HashMap<String, MessageCallOut> callOuts;

	private final ArrayList<SocketServerListener> listeners;

	private boolean started;

	private final HashMap<String, SocketDataController> controllers;

	private final Selector serverSelector;

	private final ServerSocketChannel ch;

	private final String aliasName;

	private final Timer polling;

	static {

	}

	/**
	 * Constructor.
	 * 
	 * @param protocol The protocol on this socket channel.
	 * @param port Socket port.
	 * @param manager Protocol manager.
	 * @throws Exception
	 */
	public SocketServer(
	        Protocol<SocketDataController> protocol,
	        int port,
	        MessageManager manager,
	        String aliasName) throws Exception {
		this.aliasName = aliasName;
		this.polling = new Timer();
		this.protocol = protocol;
		this.protocol.addMessageHandler(this);
		this.manager = manager;
		this.callIns = new HashMap<String, MessageCallIn<SocketDataController>>();
		this.callOuts = new HashMap<String, MessageCallOut>();
		this.started = false;
		this.controllers = new HashMap<String, SocketDataController>();
		this.listeners = new ArrayList<SocketServerListener>();

		this.serverSelector = Selector.open();

		this.ch = ServerSocketChannel.open();
		this.ch.socket().bind(new InetSocketAddress(port));
		this.ch.configureBlocking(false);
		this.ch.register(this.serverSelector, SelectionKey.OP_ACCEPT);
	}

	/**
	 * Get name.
	 * 
	 * @return The name.
	 */
	public String getName() {
		return this.aliasName;
	}

	/**
	 * Get protocol on this socket channel.
	 * 
	 * @return The protocol.
	 */
	public Protocol<SocketDataController> getProtocol() {
		return this.protocol;
	}

	/**
	 * Add a listener of states of server.
	 * 
	 * @param listener
	 */
	public void addServerListener(SocketServerListener listener) {
		this.listeners.add(listener);
	}

	/**
	 * Remove a listener of states of server.
	 * 
	 * @param listener The listener.
	 */
	public void removeServerListener(SocketServerListener listener) {
		this.listeners.remove(listener);
	}

	/**
	 * Register call in worker to handle message send from client actively.
	 * 
	 * @param callIn Call in worker.
	 */
	public void registerCallin(MessageCallIn<SocketDataController> callIn) {
		this.callIns.put(callIn.getCmdName(), callIn);
	}

	/**
	 * Send data to specific socket client.
	 * 
	 * @param clientName Client name.
	 * @param data Data.
	 */
	public boolean send(final String clientName, final byte[] data) {
		return send(clientName, data, 1);
	}

	/**
	 * Send data to specific socket client.
	 * 
	 * @param clientName Client name.
	 * @param data Data.
	 */
	public boolean send(final String clientName, final byte[] data, int times) {
		final SocketDataController controller = this.controllers.get(clientName);
		if (controller == null) {
			return false;
		}

		return controller.send(data, times);
	}

	/**
	 * Send data to specific socket client.
	 * 
	 * @param clientName Client name.
	 * @param data Data
	 * @param callOut Reply data worker.
	 * @param timeout Timeout seconds.
	 */
	public boolean send(final String clientName, final byte[] data, final MessageCallOut callOut, final long timeout) {
		final SocketDataController controller = this.controllers.get(clientName);
		if (controller == null) {
			return false;
		}

		final String tx = callOut.getTxId();
		this.callOuts.put(tx, callOut);

		if (controller.send(data, 1)) {
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {

				@Override
				public void run() {
					synchronized (SocketServer.this.callOuts) {
						if (SocketServer.this.callOuts.containsKey(tx)) {
							SocketServer.this.callOuts.remove(tx);
							callOut.timeout();
						}
					}
				}

			}, timeout);
			return true;
		} else {
			this.callOuts.remove(tx);
			return false;
		}
	}

	/**
	 * Start this server.
	 */
	public boolean start() {
		if (this.started) {
			return false;
		}

		this.started = true;

		// handle connect and receive data
		new Thread(new Runnable() {
			@Override
			public void run() {
				running();
			}
		}).start();

		// polling
		/**
		 * this.polling.schedule(new TimerTask() {
		 * 
		 * @Override public void run() { polling(); }
		 * 
		 *           }, 5000, 60000);
		 */

		return true;
	}

	/**
	 * Disconnect specific socket client.
	 * 
	 * @param clientName Client name.
	 */
	public void disconnect(String clientName) {
		SocketDataController controller = null;
		synchronized (this.controllers) {
			controller = this.controllers.remove(clientName);
		}
		if (controller != null) {
			logger.debug(String.format("%s> %s disconnected", this.aliasName, clientName));
			controller.stop();
			raiseDisconnected(controller);
		}
	}

	/**
	 * Stop this server. It always stop all clients connected.
	 */
	public void stop() {
		this.started = false;
		this.polling.cancel();

		try {
			this.serverSelector.wakeup();
			for (SocketDataController controller : this.controllers.values()) {
				SelectionKey key = controller.getChannel().keyFor(this.serverSelector);
				if (key != null) {
					key.cancel();
				} else {
					controller.stop();
				}
				raiseDisconnected(controller);
			}
		} catch (Exception ex) {

		} finally {
			this.controllers.clear();
		}
	}

	@Override
	public void messageReceived(final ProtocolMonitor<SocketDataController> monitor, final ProtocolEventArgs args) {
		final byte[] received = args.getData();

		// get command
		String cmd = this.manager.findCmd(received);
		if (cmd == null) {
			logger.debug(String.format("%s> %s> %s cmd:%s missing",
			        this.aliasName,
			        monitor.getName(),
			        monitor.getProtocol().getAliasName(),
			        cmd));
			return;
		}

		if (this.manager.isCallIn(cmd)) {
			final MessageCallIn<SocketDataController> callIn = this.callIns.get(cmd);
			if (callIn == null) {
				logger.debug(String.format("%s> %s> %s cmd:%s callIn missing",
				        this.aliasName,
				        monitor.getName(),
				        monitor.getProtocol().getAliasName(),
				        cmd));
				return;
			}

			logger.debug(String.format("%s> %s> %s cmd:%s callIn",
			        this.aliasName,
			        monitor.getName(),
			        monitor.getProtocol().getAliasName(),
			        cmd));
			new Thread(new Runnable() {

				@Override
				public void run() {
					callIn.execute(received, monitor.getController());
				}

			}).start();
		}
		else {
			String tx = this.manager.findTx(received);
			final MessageCallOut callOut = this.callOuts.get(tx);
			if (callOut == null) {
				logger.debug(String.format("%s> %s> %s cmd:%s tx:%s callOut reply missing",
				        this.aliasName,
				        monitor.getName(),
				        monitor.getProtocol().getAliasName(),
				        cmd,
				        tx));
				return;
			}

			synchronized (this.callOuts) {
				this.callOuts.remove(tx);
			}

			logger.debug(String.format("%s> %s> %s cmd:%s tx:%s callOut reply",
			        this.aliasName,
			        monitor.getName(),
			        monitor.getProtocol().getAliasName(),
			        cmd,
			        tx));
			new Thread(new Runnable() {

				@Override
				public void run() {
					callOut.execute(received);
				}

			}).start();
		}
	}

	@Override
	public void messageError(ProtocolMonitor<SocketDataController> monitor, ProtocolEventArgs args) {
		logger.debug(String.format("%s> %s> %s pack message error",
		        this.aliasName,
		        monitor.getName(),
		        monitor.getProtocol().getAliasName()));
		logger.debug(ByteUtils.toHexString(args.getData(), "-"));
	}

	private void polling() {
		if (this.started) {
			ArrayList<String> keys = new ArrayList<String>();
			synchronized (this.controllers) {
				for (Map.Entry<String, SocketDataController> kvp : this.controllers.entrySet()) {
					try {
						// kvp.getValue().send(new byte[] { 0x00 });
					} catch (Exception ex) {
						keys.add(kvp.getKey());
					}
				}
			}
			logger.info(String.format("%s> polling(%d)", this.aliasName, this.controllers.size()));

			for (String key : keys) {
				disconnect(key);
			}
		} else {
			this.polling.cancel();
		}
	}

	private void running() {
		while (this.started) {
			try {
				this.serverSelector.select(); // wait NIO event.
			} catch (Exception ex) {
				continue;
			}

			Iterator<SelectionKey> iter = this.serverSelector.selectedKeys().iterator();
			while (iter.hasNext()) {
				SelectionKey key = iter.next();
				iter.remove();

				if (key.isAcceptable()) {
					clientConnected((ServerSocketChannel) key.channel());
				} else if (key.isReadable()) {
					SocketDataController controller = (SocketDataController) key.attachment();
					try {
						controller.receive();
					} catch (Exception ex) {

					}
				}
			}
		}
	}

	private void clientConnected(ServerSocketChannel server) {
		try {
			SocketChannel client = server.accept();
			client.configureBlocking(false);

			String clientId = client.socket().getRemoteSocketAddress().toString();
			logger.info(String.format("%s> %s connected", this.aliasName, clientId));

			SocketDataController controller = null;
			synchronized (this.controllers) {
				controller = this.controllers.remove(clientId);
			}
			if (controller != null) {
				try {
					controller.stop();
				} finally {
					logger.info(String.format("%s> %s controller removed", this.aliasName, clientId));
				}
			}

			controller = new SocketDataController(clientId, client, this.protocol.createMonitor(clientId));
			synchronized (this.controllers) {
				this.controllers.put(clientId, controller);
			}

			// use internal selector
			// controller.start();

			// use server selector
			client.register(this.serverSelector, SelectionKey.OP_READ, controller);
			raiseConnected(controller);

			logger.info(String.format("%s> %s controller added", this.aliasName, clientId));
		} catch (Exception ex) {
			logger.error(String.format("%s> client connected failure. ex:%s", this.aliasName, ex.getMessage()));
		}
	}

	private void raiseConnected(SocketDataController controller) {
		for (SocketServerListener listener : this.listeners) {
			listener.connected(controller);
		}
	}

	private void raiseDisconnected(SocketDataController controller) {
		for (SocketServerListener listener : this.listeners) {
			listener.disconnected(controller);
		}
	}
}
