package uia.comm.protocol;

import java.util.List;

import org.apache.log4j.Logger;

/**
 * 
 * @author Kyle
 * 
 * @param <T>
 */
public class MultiProtocol<T> extends AbstractProtocol<T> {

	private final static Logger logger = Logger.getLogger(MultiProtocol.class);

	final List<Protocol<MultiProtocolMonitor<T>>> protocols;

	/**
	 * 
	 * @param protocols Protocols combined together.
	 */
	public MultiProtocol(final List<Protocol<MultiProtocolMonitor<T>>> protocols) {
		this.protocols = protocols;
		for (Protocol<MultiProtocolMonitor<T>> protocol : this.protocols) {
			protocol.addMessageHandler(new ProtocolEventHandler<MultiProtocolMonitor<T>>() {

				@Override
				public void messageReceived(ProtocolMonitor<MultiProtocolMonitor<T>> monitor, ProtocolEventArgs args) {
					raiseMessageReceived(monitor.getController(), args);
					monitor.getController().reset(monitor);
				}

				@Override
				public void messageError(ProtocolMonitor<MultiProtocolMonitor<T>> monitor, ProtocolEventArgs args) {
					if (MultiProtocol.this.protocols.indexOf(monitor.getController()) == 0) {
						raiseBorken(monitor.getController(), args);
					} else {
						/**
						 * logger.debug(String.format("multiProtocol:%s pack message error", monitor.getProtocol().getAliasName()));
						 */
					}
				}

			});
		}
	}

	@Override
	public ProtocolMonitor<T> createMonitor(String name) {
		return new MultiProtocolMonitor<T>(name, this);
	}

}
