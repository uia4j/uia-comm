package uia.comm;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * 
 * @author Kyle
 * 
 */
public class SocketDataSelector {

	private boolean started;

	private final Selector selector;

	/**
	 * 
	 * @throws IOException
	 */
	public SocketDataSelector() throws IOException {
		this.selector = Selector.open();
	}

	/**
	 * 
	 * @param ch
	 * @param controller
	 * @throws ClosedChannelException
	 */
	public void register(SocketChannel ch, SocketDataController controller) throws ClosedChannelException {
		ch.register(this.selector, SelectionKey.OP_READ, controller);
	}

	/**
	 * 
	 */
	public void start() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				running();
			}

		}).start();
	}

	/**
	 * 
	 */
	public void stop() {
		this.started = false;
		this.selector.wakeup();
	}

	private void running() {
		while (this.started) {
			try {
				this.selector.select();
			} catch (Exception ex) {
			}

			Iterator<SelectionKey> iter = this.selector.selectedKeys().iterator();
			while (iter.hasNext()) {
				SelectionKey key = iter.next();
				iter.remove();

				if (key.isReadable()) {
					SocketDataController controller = (SocketDataController) key.attachment();
					try {
						controller.receive();
					} catch (IOException e) {
						// TODO: raise event to disconnect
					}
				}
			}
		}
	}
}
