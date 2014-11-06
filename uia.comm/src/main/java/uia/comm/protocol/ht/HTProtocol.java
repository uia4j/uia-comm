package uia.comm.protocol.ht;

import uia.comm.protocol.AbstractProtocol;
import uia.comm.protocol.ProtocolMonitor;

public class HTProtocol<C> extends AbstractProtocol<C> {

	final byte[] head;

	final byte[] tail;

	public HTProtocol(byte[] head, byte[] tail) {
		this.head = head;
		this.tail = tail;
	}

	@Override
	public ProtocolMonitor<C> createMonitor(String name) {
		HTProtocolMonitor<C> monitor = new HTProtocolMonitor<C>(name, this);
		monitor.setProtocol(this);
		return monitor;
	}

}
