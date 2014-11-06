package uia.comm.protocol.ho;

public class IdleState<T> implements HOState<T> {

	@Override
	public void accept(HOProtocolMonitor<T> monitor, byte one) {
		if (one == monitor.protocol.head[0]) {
			monitor.reset();
			monitor.setState(new HeadState<T>());
			monitor.read(one);
		}
	}

	@Override
	public void end(HOProtocolMonitor<T> monitor) {
	}
}
