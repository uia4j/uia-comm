package uia.comm.protocol.ho;

import uia.comm.protocol.ProtocolEventArgs;

public class BodyState<T> implements HOState<T> {

	private int headIdx;

	public BodyState() {
		this.headIdx = 0;
	}

	@Override
	public void accept(HOProtocolMonitor<T> monitor, byte one) {
		if (one == monitor.protocol.head[0] && monitor.protocol.maxLength == 0) {
			monitor.finsihPacking();
			monitor.setState(new IdleState<T>());
			monitor.read(one);
			return;
		}

		if (one == monitor.protocol.head[this.headIdx]) {
			this.headIdx++;
		}
		else {
			this.headIdx = 0;
		}

		if (this.headIdx > 0 && this.headIdx == monitor.protocol.head.length)
		{
			this.headIdx = 0;
			monitor.cancelPacking(ProtocolEventArgs.ErrorCode.ERR_BODY_LENGTH);
			monitor.reset();
			for (byte b : monitor.protocol.head)
			{
				monitor.addOne(b);
			}
		}
		else {
			monitor.addOne(one);
			if (monitor.protocol.maxLength > 0 && monitor.getDataLength() == monitor.protocol.maxLength) {
				monitor.finsihPacking();
				monitor.setState(new IdleState<T>());
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
