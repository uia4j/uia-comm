package uia.comm.protocol;

import java.util.ArrayList;

public abstract class AbstractProtocol<C> implements Protocol<C> {

	private final ArrayList<ProtocolEventHandler<C>> handlers;

	private String aliasName;

	public AbstractProtocol() {
		this.handlers = new ArrayList<ProtocolEventHandler<C>>();
	}

	public String getAliasName() {
		return this.aliasName;
	}

	public void setAliasName(String aliasName) {
		this.aliasName = aliasName;
	}

	@Override
	public synchronized void addMessageHandler(ProtocolEventHandler<C> handler) {
		this.handlers.add(handler);
	}

	@Override
	public synchronized void remmoveMessageHandler(ProtocolEventHandler<C> handler) {
		this.handlers.remove(handler);
	}

	public synchronized void raiseMessageReceived(ProtocolMonitor<C> monitor, ProtocolEventArgs args) {
		for (ProtocolEventHandler<C> h : this.handlers) {
			try {
				h.messageReceived(monitor, args);
			} catch (Exception ex) {

			}
		}
	}

	public synchronized void raiseBorken(ProtocolMonitor<C> monitor, ProtocolEventArgs args) {
		for (ProtocolEventHandler<C> h : this.handlers) {
			try {
				h.messageError(monitor, args);
			} catch (Exception ex) {

			}
		}
	}
}
