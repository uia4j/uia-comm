package uia.comm.protocol;

import java.util.ArrayList;

import org.junit.Test;

import uia.comm.protocol.hl.HLProtocol;
import uia.comm.protocol.ho.HOProtocol;
import uia.comm.protocol.ht.HTProtocol;
import uia.utils.ByteUtils;

public class MultiProtocolTest implements ProtocolEventHandler<Object> {

    private final MultiProtocol<Object> protocol;

    public MultiProtocolTest() {
        HOProtocol<MultiProtocolMonitor<Object>> p1 = new HOProtocol<MultiProtocolMonitor<Object>>(
                new byte[] { (byte) 0x41, (byte) 0x42, (byte) 0x43 },
                5);

        HTProtocol<MultiProtocolMonitor<Object>> p2 = new HTProtocol<MultiProtocolMonitor<Object>>(
                new byte[] { (byte) 0x8a, (byte) 0x8a },
                new byte[] { (byte) 0xa8, (byte) 0xa8 });

        ArrayList<Protocol<MultiProtocolMonitor<Object>>> ps = new ArrayList<Protocol<MultiProtocolMonitor<Object>>>();
        ps.add(p1);
        ps.add(p2);

        this.protocol = new MultiProtocol<Object>(ps);
        this.protocol.addMessageHandler(this);
    }

    @Test
    public void testNormal() {
        ProtocolMonitor<Object> monitor = this.protocol.createMonitor("abc");
        monitor.read((byte) 0x41);
        monitor.read((byte) 0x42);
        monitor.read((byte) 0x43);
        monitor.read((byte) 0x44);
        monitor.read((byte) 0x45);

        monitor.read((byte) 0x8a);
        monitor.read((byte) 0x8a);
        monitor.read((byte) 0x44);
        monitor.read((byte) 0x45);
        monitor.read((byte) 0x46);
        monitor.read((byte) 0xa8);
        monitor.read((byte) 0xa8);
    }

    @Test
    public void testEx1() {
        ProtocolMonitor<Object> monitor = this.protocol.createMonitor("abc");
        monitor.read((byte) 0x8a);
        monitor.read((byte) 0x8a);

        monitor.read((byte) 0x41);
        monitor.read((byte) 0x42);
        monitor.read((byte) 0x43);
        monitor.read((byte) 0x44);
        monitor.read((byte) 0x45);

        monitor.read((byte) 0x46);
        monitor.read((byte) 0x8a);
        monitor.read((byte) 0x8a);
        monitor.read((byte) 0x47);
        monitor.read((byte) 0x48);
        monitor.read((byte) 0xa8);
        monitor.read((byte) 0xa8);
    }

    @Test
    public void testCase1() {
        // SOH
        HLProtocol<MultiProtocolMonitor<Object>> soh =
                new HLProtocol<MultiProtocolMonitor<Object>>(
                        5,
                        1,
                        3,
                        2,
                        new LenReader() {

                            @Override
                            public int read(byte[] data) {
                                return ByteUtils.shortValue(data);
                            }

                        },
                        new byte[] { 0x10, 0x01 });
        soh.setAliasName("SOH");
        // ACK
        HOProtocol<MultiProtocolMonitor<Object>> ack =
                new HOProtocol<MultiProtocolMonitor<Object>>(
                        new byte[] { 0x10, 0x06 },
                        4);
        ack.setAliasName("ACK");
        // NAK
        HOProtocol<MultiProtocolMonitor<Object>> nak =
                new HOProtocol<MultiProtocolMonitor<Object>>(
                        new byte[] { 0x10, 0x15 },
                        5);
        nak.setAliasName("NAK");

        ArrayList<Protocol<MultiProtocolMonitor<Object>>> sub =
                new ArrayList<Protocol<MultiProtocolMonitor<Object>>>();
        sub.add(soh);
        sub.add(ack);
        sub.add(nak);

        MultiProtocol<Object> protocol = new MultiProtocol<Object>(sub);
        protocol.addMessageHandler(this);

        ProtocolMonitor<Object> monitor = protocol.createMonitor("THSRC");
        monitor.read((byte) 0x10);
        monitor.read((byte) 0x06);
        monitor.read((byte) 0x0f);
        monitor.read((byte) 0x19);
        monitor.read((byte) 0x10);
        monitor.read((byte) 0x01);
        monitor.read((byte) 0x10);
        monitor.read((byte) 0x45);
    }

    @Override
    public void messageReceived(ProtocolMonitor<Object> monitor, ProtocolEventArgs args) {
        System.out.println("r:len=" + args.getData().length + ", " + ByteUtils.toHexString(args.getData()));
    }

    @Override
    public void messageError(ProtocolMonitor<Object> monitor, ProtocolEventArgs args) {
        System.out.println("e:" + args.getErrorCode() + ",len=" + args.getData().length + ", " + ByteUtils.toHexString(args.getData()));
    }
}
