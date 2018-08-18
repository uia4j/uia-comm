package uia.comm.protocol.xml;

/**
 *
 * @author Kyle K. Lin
 *
 * @param <C>
 */
public interface XMLState<C> {

    public void accept(XMLProtocolMonitor<C> monitor, byte one);

}
