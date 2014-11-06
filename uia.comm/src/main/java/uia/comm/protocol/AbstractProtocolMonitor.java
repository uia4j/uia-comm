package uia.comm.protocol;

import java.util.ArrayList;

public abstract class AbstractProtocolMonitor<C> implements ProtocolMonitor<C> {

	protected final ArrayList<Byte> data;

	private final String name;

	private C controller;

	private Protocol<C> protocol;

	public AbstractProtocolMonitor(String name) {
		this.name = name;
		this.data = new ArrayList<Byte>();
	}

	public Protocol<C> getProtocol() {
		return this.protocol;
	}

	public void setProtocol(Protocol<C> protocol) {
		this.protocol = protocol;
	}

	public int getDataLength() {
		return this.data.size();
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void readEnd() {
	}

	@Override
	public void reset() {
		this.data.clear();
	}

	@Override
	public C getController() {
		return this.controller;
	}

	@Override
	public void setController(C controller) {
		this.controller = controller;
	}

	public byte[] packing() {
		byte[] result = new byte[this.data.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = this.data.get(i);
		}
		return result;
	}
}
