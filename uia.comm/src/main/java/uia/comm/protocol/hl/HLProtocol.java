package uia.comm.protocol.hl;

import uia.comm.protocol.AbstractProtocol;
import uia.comm.protocol.LenReader;
import uia.comm.protocol.ProtocolMonitor;

public class HLProtocol<T> extends AbstractProtocol<T> {

    final int lenStartOffset;

    final int lenEndOffset;

    final int lenFieldStartIdx;

    final int lenFieldByteCount;

    final LenReader reader;

    final byte[] head;

    final boolean strict;

    public HLProtocol(
            int lenStartOffset,
            int lenEndOffset,
            int lenFieldIdx,
            int lenFieldCount,
            LenReader reader,
            byte[] head) {
        this(lenStartOffset, lenEndOffset, lenFieldIdx, lenFieldCount, reader, head, true);
    }

    public HLProtocol(
            int lenStartOffset,
            int lenEndOffset,
            int lenFieldIdx,
            int lenFieldCount,
            LenReader reader,
            byte[] head,
            boolean strict) {
        this.lenStartOffset = lenStartOffset;
        this.lenEndOffset = lenEndOffset;
        this.lenFieldStartIdx = lenFieldIdx;
        this.lenFieldByteCount = lenFieldCount;
        this.reader = reader;
        this.head = head;
        this.strict = strict;
    }

    public int getLenFieldStartIdx() {
        return this.lenFieldStartIdx;
    }

    public int getLenFieldEndIdx() {
        return this.lenFieldStartIdx + this.lenFieldByteCount;
    }

    @Override
    public ProtocolMonitor<T> createMonitor(String name) {
        HLProtocolMonitor<T> monitor = new HLProtocolMonitor<T>(name, this);
        monitor.setProtocol(this);
        return monitor;
    }

}
