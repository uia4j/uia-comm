package uia.comm.protocol;

import org.junit.Test;

import uia.comm.protocol.ht.HTProtocol;

public class HTProtocolTest implements ProtocolEventHandler<Object> {

	private final HTProtocol<Object> protocol;

	public HTProtocolTest() {
		this.protocol = new HTProtocol<Object>(
		        new byte[] { (byte) 0x8a, (byte) 0x8a },
		        new byte[] { (byte) 0xa8, (byte) 0xa8 });
		this.protocol.addMessageHandler(this);
	}

	@Test
	public void testNormal() {

		ProtocolMonitor<Object> monitor = this.protocol.createMonitor("abc");
		monitor.read((byte) 0x8a);
		monitor.read((byte) 0x8a);
		monitor.read((byte) 0x41);
		monitor.read((byte) 0x43);
		monitor.read((byte) 0x45);
		monitor.read((byte) 0xa8);
		monitor.read((byte) 0xa8);
	}

	@Test
	public void testEx1() {

		ProtocolMonitor<Object> monitor = this.protocol.createMonitor("abc");
		monitor.read((byte) 0x8a);
		monitor.read((byte) 0x8a);
		monitor.read((byte) 0x41);
		monitor.read((byte) 0x43);

		monitor.read((byte) 0x8a);
		monitor.read((byte) 0x8a);
		monitor.read((byte) 0x45);
		monitor.read((byte) 0xa8);
		monitor.read((byte) 0xa8);
	}

	@Test
	public void testEx2() {

		ProtocolMonitor<Object> monitor = this.protocol.createMonitor("abc");
		monitor.read((byte) 0x8a);
		monitor.read((byte) 0x8a);
		monitor.read((byte) 0x41);
		monitor.read((byte) 0xa8);

		monitor.read((byte) 0x8a);
		monitor.read((byte) 0x8a);
		monitor.read((byte) 0x43);
		monitor.read((byte) 0x45);
		monitor.read((byte) 0xa8);
		monitor.read((byte) 0xa8);
	}

	@Override
	public void messageReceived(ProtocolMonitor<Object> monitor, ProtocolEventArgs args) {
		System.out.println("r:" + new String(args.getData()));
	}

	@Override
	public void messageError(ProtocolMonitor<Object> monitor, ProtocolEventArgs args) {
		System.out.println("e:" + new String(args.getData()));
	}
}
