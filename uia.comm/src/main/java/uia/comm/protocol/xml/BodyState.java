package uia.comm.protocol.xml;

import uia.comm.protocol.ProtocolEventArgs;

/**
 *
 * @author Kyle K. Lin
 *
 * @param <C> The reference data type.
 */
public class BodyState<C> implements XMLState<C> {

    private int headIdx;

    private int tailIdx;

    public BodyState() {
    }

    @Override
    public String toString() {
        return "BodyState";
    }

    @Override
    public void accept(XMLProtocolMonitor<C> monitor, byte one) {
        if (one == monitor.protocol.head[this.headIdx]) {
            this.headIdx++;
        }
        else {
            this.headIdx = 0;
        }
        if (one == monitor.protocol.tail[this.tailIdx]) {
            this.tailIdx++;
        }
        else {
            this.tailIdx = 0;
        }

        if (this.headIdx > 0 && this.headIdx == monitor.protocol.head.length) {
            this.headIdx = 0;
            monitor.addOne(one);
            monitor.cancelPacking(ProtocolEventArgs.ErrorCode.ERR_HEAD_REPEAT);
            for (byte b : monitor.protocol.head) {
                monitor.addOne(b);
            }
        }
        else if (this.tailIdx > 0 && this.tailIdx == monitor.protocol.tail.length) {
            monitor.addOne(one);
            monitor.finsihPacking();
            monitor.setState(new IdleState<C>());
        }
        else {
            monitor.addOne(one);
        }

    }
}
