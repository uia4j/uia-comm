package uia.comm.protocol.ht;

import uia.comm.protocol.ProtocolEventArgs;

public class BodyState<C> implements HTState<C> {

    private int headIdx;

    public BodyState() {
        this.headIdx = 0;
    }

    @Override
    public void accept(HTProtocolMonitor<C> monitor, byte one) {
        if (one == monitor.protocol.head[this.headIdx])
        {
            this.headIdx++;
        }
        else
        {
            this.headIdx = 0;
        }

        if (this.headIdx > 0 && this.headIdx == monitor.protocol.head.length)
        {
            this.headIdx = 0;
            monitor.addOne(one);
            monitor.cancelPacking(ProtocolEventArgs.ErrorCode.ERR_HEAD_REPEAT);
            for (byte b : monitor.protocol.head)
            {
                monitor.addOne(b);
            }
        }
        else {
            if (one == monitor.protocol.tail[0]) {
                monitor.setState(new TailState<C>());
                monitor.read(one);
            }
            else {
                monitor.addOne(one);
            }
        }
    }
}
