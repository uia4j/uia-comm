package uia.comm.protocol.ht;

import uia.comm.protocol.AbstractProtocolMonitor;
import uia.comm.protocol.ProtocolEventArgs;

public class HTProtocolMonitor<C> extends AbstractProtocolMonitor<C> {

	int headIdx;

	int tailIdx;

	HTState<C> state;

	final HTProtocol<C> protocol;

	public HTProtocolMonitor(String name, HTProtocol<C> protocol) {
		super(name);
		this.state = new IdleState<C>();
		this.protocol = protocol;
	}

	@Override
	public void read(byte one) {
		this.state.accept(this, one);
	}

	@Override
	public void reset() {
		this.headIdx = 0;
		this.tailIdx = 0;
		this.data.clear();
		this.state = new IdleState<C>();
	}

	void setState(HTState<C> state) {
		this.state = state;
	}

	void addOne(byte one) {
		this.data.add(one);
	}

	void finsihPacking() {
		ProtocolEventArgs args = new ProtocolEventArgs(packing());
		this.protocol.raiseMessageReceived(this, args);
	}

	void cancelPacking(ProtocolEventArgs.ErrorCode errorCode) {
		ProtocolEventArgs args = new ProtocolEventArgs(packing(), errorCode);
		this.protocol.raiseBorken(this, args);
	}
}
