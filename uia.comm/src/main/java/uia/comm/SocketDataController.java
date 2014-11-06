package uia.comm;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import uia.comm.protocol.ProtocolMonitor;

/**
 * 
 * @author Kyle
 * 
 */
public class SocketDataController {

	private final static Logger logger = Logger.getLogger(SocketDataController.class);

	private final String name;;

	private boolean started;

	private final Selector selector;

	private final ProtocolMonitor<SocketDataController> monitor;

	private SocketChannel ch;

	// private final Timer idleTimer;

	/**
	 * 
	 * @param name
	 * @param ch
	 * @param monitor
	 * @throws IOException
	 */
	SocketDataController(String name, SocketChannel ch, ProtocolMonitor<SocketDataController> monitor) throws IOException {
		this.name = name;
		this.started = false;
		this.selector = Selector.open();
		this.ch = ch;
		this.ch.configureBlocking(false);
		this.ch.register(this.selector, SelectionKey.OP_READ);
		this.monitor = monitor;
		this.monitor.setController(this);
		//this.idleTimer = new Timer();
		/**
		this.idleTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				idleOut();
			}

		}, 120000, 120000);
		*/
	}

	public String getName() {
		return this.name;
	}

	/**
	 * Send data to remote.
	 * 
	 * @param data Data.
	 * @param times Retry times.
	 * @return Success or not.
	 */
	public synchronized boolean send(byte[] data, int times) {
		while (times > 0) {
			try {
				int cnt = this.ch.write(ByteBuffer.wrap(data));
				if (cnt == data.length) {
					return true;
				} else {
					logger.equals("write count error!!");
				}
			} catch (Exception ex) {
				logger.error(ex);
			} finally {
				times--;
			}
		}
		return false;
	}

	/**
	 * Start this controller using internal selector.
	 * 
	 * @return true if start success first time.
	 */
	synchronized boolean start() {
		if (this.ch == null || this.started) {
			return false;
		}
		this.started = true;
		new Thread(new Runnable() {

			@Override
			public void run() {
				running();
			}

		}).start();
		return true;
	}

	/**
	 * 
	 */
	synchronized void stop() {
		if (this.ch != null) {
			try {
				this.ch.close();
				this.ch.keyFor(this.selector).cancel();
			} catch (Exception ex) {

			}
		}
		this.ch = null;
		this.started = false;
	}

	synchronized void receive() throws IOException {
		if (this.ch == null) {
			return;
		}

		//this.idleTimer.cancel();

		int len = 0;
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		do {
			len = this.ch.read(buffer);
			if (len > 0) {
				byte[] value = (byte[]) buffer.flip().array();
				value = Arrays.copyOf(value, len);
				for (byte b : value) {
					this.monitor.read(b);
				}
			}
			buffer.clear();
		} while (len > 0);
		this.monitor.readEnd();

		/**
		this.idleTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				idleOut();
			}

		}, 120000, 120000);
		*/
	}

	SocketChannel getChannel() {
		return this.ch;
	}

	private void idleOut() {
		//this.idleTimer.cancel();
	}

	/**
	 * use internal selector to handle received data.
	 * 
	 */
	private void running() {
		while (this.started) {
			try {
				this.selector.select(); // wait NIO event
			} catch (Exception ex) {
				continue;
			}

			Iterator<SelectionKey> iterator = this.selector.selectedKeys().iterator();
			while (iterator.hasNext()) {
				SelectionKey selectionKey = iterator.next();
				@SuppressWarnings("unused")
				SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
				iterator.remove();

				try {
					receive();
				} catch (IOException e) {

				}
			}
		}
	}
}
