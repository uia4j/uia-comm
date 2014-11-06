package uia.comm.protocol;

import java.util.ArrayList;

/**
 * 
 * @author Kyle
 * 
 * @param <T>
 */
public class MultiProtocolMonitor<T> implements ProtocolMonitor<T> {

	private final String name;

	private final ArrayList<ProtocolMonitor<MultiProtocolMonitor<T>>> monitors;

	private final MultiProtocol<T> protocol;

	private T controller;

	/**
	 * 
	 * @param name
	 * @param protocol
	 */
	public MultiProtocolMonitor(String name, MultiProtocol<T> protocol) {
		this.protocol = protocol;
		this.name = name;
		this.monitors = new ArrayList<ProtocolMonitor<MultiProtocolMonitor<T>>>();
		for (Protocol<MultiProtocolMonitor<T>> item : protocol.protocols)
		{
			ProtocolMonitor<MultiProtocolMonitor<T>> monitor = item.createMonitor(name);
			monitor.setController(this);
			this.monitors.add(monitor);
		}
	}

	@Override
	public Protocol<T> getProtocol() {
		return this.protocol;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void read(byte data) {
		for (ProtocolMonitor<MultiProtocolMonitor<T>> monitor : this.monitors) {
			monitor.read(data);
		}
	}

	@Override
	public void readEnd() {
		for (ProtocolMonitor<MultiProtocolMonitor<T>> monitor : this.monitors) {
			monitor.readEnd();
		}
	}

	@Override
	public void reset() {
		for (ProtocolMonitor<MultiProtocolMonitor<T>> monitor : this.monitors) {
			monitor.reset();
		}
	}

	@Override
	public T getController() {
		return this.controller;
	}

	@Override
	public void setController(T controller) {
		this.controller = controller;
	}

	void reset(ProtocolMonitor<MultiProtocolMonitor<T>> monitor) {
		int idx = this.monitors.indexOf(monitor);
		if (idx >= 0) {
			for (int i = idx; i < this.monitors.size(); i++) {
				this.monitors.get(i).reset();
			}
		}
	}

}
