package uia.comm.protocol.hl;

import uia.comm.protocol.ProtocolEventArgs;

public class BodyState<T> implements HLState<T> {

	private int len;

	private int headIdx;

	public BodyState() {
		this.len = -1;
		this.headIdx = 0;
	}

	@Override
	public void accept(HLProtocolMonitor<T> monitor, byte one) {
		if (one == monitor.protocol.head[this.headIdx]) {
			this.headIdx++;
		} else {
			this.headIdx = 0;
		}

		if (this.headIdx > 0 && this.headIdx == monitor.protocol.head.length) {
			this.headIdx = 0;
			monitor.cancelPacking(ProtocolEventArgs.ErrorCode.ERR_BODY);
			for (byte b : monitor.protocol.head) {
				monitor.addOne(b);
			}
		} else {
			monitor.addOne(one);
			if (monitor.getDataLength() == monitor.protocol.getLenFieldEndIdx()) {
				this.len = monitor.readLenFromLeField();
			}

			if (monitor.getDataLength() > monitor.protocol.getLenFieldEndIdx() && this.len < 0) {
				monitor.cancelPacking(ProtocolEventArgs.ErrorCode.ERR_BODY_LENGTH);
				monitor.setState(new IdleState<T>());
				return;
			}

			if (this.len >= 0 && (monitor.protocol.lenStartOffset + this.len + monitor.protocol.lenEndOffset) == monitor.getDataLength()) {
				monitor.finishPacking();
				monitor.setState(new IdleState<T>());
			}
		}
	}
}
