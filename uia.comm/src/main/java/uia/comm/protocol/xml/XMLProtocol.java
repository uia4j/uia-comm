package uia.comm.protocol.xml;

import uia.comm.protocol.AbstractProtocol;
import uia.comm.protocol.ProtocolMonitor;

/**
 *
 * @author Kyle K. Lin
 *
 * @param <C> The reference data type.
 */
public class XMLProtocol<C> extends AbstractProtocol<C> {

    final byte[] head;

    final byte[] tail;

    public XMLProtocol(String rootTag) {
        this.head = ("<" + rootTag + ">").getBytes();
        this.tail = ("</" + rootTag + ">").getBytes();
    }

    @Override
    public ProtocolMonitor<C> createMonitor(String name) {
        XMLProtocolMonitor<C> monitor = new XMLProtocolMonitor<C>(name, this);
        monitor.setProtocol(this);
        return monitor;
    }

}
