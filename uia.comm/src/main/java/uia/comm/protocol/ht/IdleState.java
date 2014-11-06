package uia.comm.protocol.ht;

public class IdleState<C> implements HTState<C> {

	@Override
	public void accept(HTProtocolMonitor<C> monitor, byte one) {
		if (one == monitor.protocol.head[0]) {
			monitor.reset();
			monitor.setState(new HeadState<C>());
			monitor.read(one);
		}
	}
}
