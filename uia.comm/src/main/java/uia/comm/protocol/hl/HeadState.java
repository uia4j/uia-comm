package uia.comm.protocol.hl;

import uia.comm.protocol.ProtocolEventArgs;

public class HeadState<T> implements HLState<T> {

	@Override
	public void accept(HLProtocolMonitor<T> monitor, byte one) {
		monitor.addOne(one);
		if (monitor.headIdx < monitor.protocol.head.length && one == monitor.protocol.head[monitor.headIdx]) {
			monitor.headIdx++;
			if (monitor.headIdx >= monitor.protocol.head.length) {
				monitor.headIdx = 0;
				monitor.setState(new BodyState<T>());
			}
		} else {
			int idx = monitor.headIdx;
			monitor.cancelPacking(ProtocolEventArgs.ErrorCode.ERR_HEAD);
			monitor.reset();
			if (idx > 0)
			{
				monitor.setState(new HeadState<T>());
				monitor.read(one);
			}
			else
			{
				monitor.setState(new IdleState<T>());
			}
		}
	}

}
