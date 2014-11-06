package uia.comm.protocol.ng;

import uia.comm.protocol.AbstractProtocolMonitor;
import uia.comm.protocol.ProtocolEventArgs;

public class NGProtocolMonitor<C> extends AbstractProtocolMonitor<C> {

	final NGProtocol<C> protocol;

	public NGProtocolMonitor(String name, NGProtocol<C> protocol) {
		super(name);
		this.protocol = protocol;
	}

	@Override
	public void read(byte one) {
		this.data.add(one);
	}

	@Override
	public void readEnd() {
		finsihPacking();
		this.data.clear();
	}

	@Override
	public void reset() {
		this.data.clear();
	}

	private void finsihPacking() {
		ProtocolEventArgs args = new ProtocolEventArgs(packing());
		this.protocol.raiseMessageReceived(this, args);
	}
}
