package uia.comm.protocol.ht;

import uia.comm.protocol.ProtocolEventArgs;

public class TailState<C> implements HTState<C> {

	@Override
	public void accept(HTProtocolMonitor<C> monitor, byte one) {
		if (one == monitor.protocol.tail[monitor.tailIdx]) {
			monitor.addOne(one);
			monitor.tailIdx++;
			if (monitor.tailIdx >= monitor.protocol.tail.length) {
				monitor.finsihPacking();
				monitor.reset();
				monitor.setState(new IdleState<C>());
			}
		} else {
			monitor.cancelPacking(ProtocolEventArgs.ErrorCode.ERR_TAIL);
			monitor.reset();
			if (one == monitor.protocol.head[0]) {
				monitor.setState(new HeadState<C>());
				monitor.read(one);
			} else {
				monitor.setState(new IdleState<C>());
			}
		}
	}
}
