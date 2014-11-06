package uia.comm.protocol.ho;

import uia.comm.protocol.AbstractProtocol;
import uia.comm.protocol.ProtocolMonitor;

public class HOProtocol<C> extends AbstractProtocol<C> {

	final byte[] head;

	final int maxLength;

	public HOProtocol(byte[] head) {
		this(head, 0);
	}

	public HOProtocol(byte[] head, int maxLength) {
		this.head = head;
		this.maxLength = maxLength;
	}

	@Override
	public ProtocolMonitor<C> createMonitor(String name) {
		HOProtocolMonitor<C> monitor = new HOProtocolMonitor<C>(name, this);
		monitor.setProtocol(this);
		return monitor;
	}

}
