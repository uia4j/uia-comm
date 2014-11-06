package uia.comm.protocol.ho;

import uia.comm.protocol.AbstractProtocolMonitor;
import uia.comm.protocol.ProtocolEventArgs;

public class HOProtocolMonitor<C> extends AbstractProtocolMonitor<C> {

	final HOProtocol<C> protocol;

	int headIdx;

	private HOState<C> state;

	public HOProtocolMonitor(String name, HOProtocol<C> protocol) {
		super(name);
		this.protocol = protocol;
		this.state = new IdleState<C>();
	}

	@Override
	public void read(byte one) {
		this.state.accept(this, one);
	}

	@Override
	public void readEnd() {
		this.state.end(this);
	}

	@Override
	public void reset() {
		this.headIdx = 0;
		this.data.clear();
	}

	void setState(HOState<C> state) {
		this.state = state;
	}

	void addOne(byte one) {
		this.data.add(one);
	}

	void finsihPacking() {
		ProtocolEventArgs args = new ProtocolEventArgs(packing());
		reset();
		this.protocol.raiseMessageReceived(this, args);
	}

	void cancelPacking(ProtocolEventArgs.ErrorCode errorCode) {
		ProtocolEventArgs args = new ProtocolEventArgs(packing(), errorCode);
		reset();
		this.protocol.raiseBorken(this, args);
	}
}
