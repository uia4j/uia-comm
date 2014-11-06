package uia.comm.protocol.hl;

import uia.comm.protocol.AbstractProtocolMonitor;
import uia.comm.protocol.ProtocolEventArgs;

public class HLProtocolMonitor<T> extends AbstractProtocolMonitor<T> {

	int headIdx;

	final HLProtocol<T> protocol;

	private HLState<T> state;

	public HLProtocolMonitor(String name, HLProtocol<T> protocol) {
		super(name);

		this.protocol = protocol;
		this.state = new IdleState<T>();
	}

	public HLState<T> getState()
	{
		return this.state;
	}

	public void setState(HLState<T> state) {
		this.state = state;
	}

	@Override
	public void read(byte one) {
		this.state.accept(this, one);
	}

	@Override
	public void reset() {
		this.headIdx = 0;
		this.state = new IdleState<T>();
		this.data.clear();
	}

	int readLenFromLeField() {
		byte[] data = new byte[this.protocol.lenFieldByteCount];
		for (int i = 0; i < data.length; i++)
		{
			data[i] = this.data.get(this.protocol.lenFieldStartIdx + i);
		}

		return this.protocol.reader.read(data);
	}

	void addOne(byte one) {
		this.data.add(one);
	}

	void cancelPacking(ProtocolEventArgs.ErrorCode errorCode)
	{
		ProtocolEventArgs args = new ProtocolEventArgs(packing(), errorCode);
		this.data.clear();
		this.protocol.raiseBorken(this, args);
	}

	void finishPacking()
	{
		ProtocolEventArgs args = new ProtocolEventArgs(packing());
		this.data.clear();
		this.protocol.raiseMessageReceived(this, args);
	}
}
