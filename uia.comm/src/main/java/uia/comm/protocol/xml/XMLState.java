package uia.comm.protocol.xml;

/**
 *
 * @author Kyle K. Lin
 *
 * @param <C> The reference data type.
 */
public interface XMLState<C> {

    public void accept(XMLProtocolMonitor<C> monitor, byte one);

}
