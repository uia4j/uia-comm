package uia.comm.protocol.xml;

/**
 *
 * @author Kyle K. Lin
 *
 * @param <C> The reference data type.
 */
public class IdleState<C> implements XMLState<C> {

    @Override
    public String toString() {
        return "IdleState";
    }

    @Override
    public void accept(XMLProtocolMonitor<C> monitor, byte one) {
        if (one == monitor.protocol.head[0]) {
            monitor.reset();
            monitor.setState(new HeadState<C>());
            monitor.read(one);
        }
    }
}
