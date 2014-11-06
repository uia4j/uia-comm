package uia.comm.protocol.ng;

import uia.comm.protocol.AbstractProtocol;
import uia.comm.protocol.ProtocolMonitor;

public class NGProtocol<C> extends AbstractProtocol<C> {

	public NGProtocol() {
	}

	@Override
	public ProtocolMonitor<C> createMonitor(String name) {
		NGProtocolMonitor<C> monitor = new NGProtocolMonitor<C>(name, this);
		monitor.setProtocol(this);
		return monitor;
	}

}
