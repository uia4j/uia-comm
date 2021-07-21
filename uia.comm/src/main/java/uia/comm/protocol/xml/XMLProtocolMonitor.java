package uia.comm.protocol.xml;

import uia.comm.protocol.AbstractProtocolMonitor;
import uia.comm.protocol.ProtocolEventArgs;

/**
 *
 * @author Kyle K. Lin
 *
 * @param <C> The reference data type.
 */
public class XMLProtocolMonitor<C> extends AbstractProtocolMonitor<C> {

    int headIdx;

    int tailIdx;

    XMLState<C> state;

    final XMLProtocol<C> protocol;

    public XMLProtocolMonitor(String name, XMLProtocol<C> protocol) {
        super(name);
        this.state = new IdleState<C>();
        this.protocol = protocol;
    }

    @Override
    public void read(byte one) {
        this.state.accept(this, one);
    }

    @Override
    public void reset() {
        this.headIdx = 0;
        this.tailIdx = 0;
        this.data.clear();
        this.state = new IdleState<C>();
    }

    @Override
    public boolean isRunning() {
        return !(this.state instanceof IdleState);
    }

    @Override
    public String getStateInfo() {
        return getState().toString();
    }

    public XMLState<C> getState() {
        return this.state;
    }

    public void setState(XMLState<C> state) {
        this.state = state;
    }

    void addOne(byte one) {
        this.data.add(one);
    }

    void finsihPacking() {
        ProtocolEventArgs args = new ProtocolEventArgs(packing());
        this.protocol.raiseMessageReceived(this, args);
    }

    void cancelPacking(ProtocolEventArgs.ErrorCode errorCode) {
        ProtocolEventArgs args = new ProtocolEventArgs(packing(), errorCode);
        this.protocol.raiseMessageError(this, args);
    }
}
