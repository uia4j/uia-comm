package uia.comm.protocol.hl;

public class IdleState<T> implements HLState<T> {

	@Override
	public void accept(HLProtocolMonitor<T> monitor, byte one) {
		if (one == monitor.protocol.head[0]) {
			monitor.setState(new HeadState<T>());
			monitor.read(one);
		}
	}

}
