package uia.comm.protocol.ho;

import uia.comm.protocol.ProtocolEventArgs;

public class HeadState<T> implements HOState<T> {

	@Override
	public void accept(HOProtocolMonitor<T> monitor, byte one) {
		if (one == monitor.protocol.head[monitor.headIdx]) {
			monitor.addOne(one);
			monitor.headIdx++;
			if (monitor.headIdx >= monitor.protocol.head.length) {
				if (monitor.protocol.maxLength > 0 && monitor.getDataLength() >= monitor.protocol.maxLength) {
					monitor.finsihPacking();
					monitor.setState(new IdleState<T>());
				} else {
					monitor.setState(new BodyState<T>());
				}
			}
		} else {
			if (monitor.headIdx == 0) {
				monitor.setState(new IdleState<T>());
			}
			else {
				monitor.cancelPacking(ProtocolEventArgs.ErrorCode.ERR_HEAD);
				monitor.reset();
				monitor.read(one);
			}
		}
	}

	@Override
	public void end(HOProtocolMonitor<T> monitor) {
		if (monitor.protocol.maxLength == 0) {
			monitor.finsihPacking();
			monitor.setState(new IdleState<T>());
		}
	}
}
