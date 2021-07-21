package uia.comm.protocol.xml;

/**
 *
 * @author Kyle K. Lin
 *
 * @param <C> The reference data type.
 */
public class HeadState<C> implements XMLState<C> {

    @Override
    public String toString() {
        return "HeadState";
    }

    @Override
    public void accept(XMLProtocolMonitor<C> monitor, byte one) {
        if (one == monitor.protocol.head[monitor.headIdx]) {
            monitor.addOne(one);
            monitor.headIdx++;
            if (monitor.headIdx >= monitor.protocol.head.length) {
                monitor.setState(new BodyState<C>());
            }
        }
        else {
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
