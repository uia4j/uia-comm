package uia.comm.protocol.ht;

public class HeadState<C> implements HTState<C> {

	@Override
	public void accept(HTProtocolMonitor<C> monitor, byte one) {
		if (one == monitor.protocol.head[monitor.headIdx]) {
			monitor.addOne(one);
			monitor.headIdx++;
			if (monitor.headIdx >= monitor.protocol.head.length) {
				monitor.setState(new BodyState<C>());
			}
		} else {
			if (monitor.headIdx == 0) {
				monitor.setState(new IdleState<C>());
			}
			else {
				monitor.reset();
				monitor.read(one);
			}
		}
	}
}
