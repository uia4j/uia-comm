package uia.comm.protocol;

import org.junit.Test;

import uia.comm.protocol.ho.HOProtocol;
import uia.utils.ByteUtils;

public class HOProtocolTest implements ProtocolEventHandler<Object> {

	public HOProtocolTest() {
	}

	@Test
	public void testNormal1() {
		HOProtocol<Object> protocol = new HOProtocol<Object>(
		        new byte[] { (byte) 0x41, (byte) 0x42, (byte) 0x43 },
		        5);
		protocol.addMessageHandler(this);

		ProtocolMonitor<Object> monitor = protocol.createMonitor("abc");
		monitor.read((byte) 0x41);
		monitor.read((byte) 0x42);
		monitor.read((byte) 0x43);
		monitor.read((byte) 0x44);
		monitor.readEnd();
		monitor.read((byte) 0x45);

		monitor.read((byte) 0x46);
		monitor.read((byte) 0x47);

		monitor.read((byte) 0x41);
		monitor.read((byte) 0x42);
		monitor.read((byte) 0x43);
		monitor.read((byte) 0x41);
		monitor.read((byte) 0x47);
	}

	@Test
	public void testNormal2() {
		HOProtocol<Object> protocol = new HOProtocol<Object>(
		        new byte[] { (byte) 0x41, (byte) 0x42, (byte) 0x43 },
		        3);
		protocol.addMessageHandler(this);

		ProtocolMonitor<Object> monitor = protocol.createMonitor("abc");
		monitor.read((byte) 0x41);
		monitor.read((byte) 0x42);
		monitor.read((byte) 0x43);
		monitor.read((byte) 0x44);
		monitor.read((byte) 0x45);
	}

	@Test
	public void testNormal3() {
		HOProtocol<Object> protocol = new HOProtocol<Object>(
		        new byte[] { (byte) 0x41, (byte) 0x42, (byte) 0x43 },
		        0);
		protocol.addMessageHandler(this);

		ProtocolMonitor<Object> monitor = protocol.createMonitor("abc");
		monitor.read((byte) 0x41);
		monitor.read((byte) 0x42);
		monitor.read((byte) 0x43);
		monitor.read((byte) 0x44);
		monitor.readEnd();
		monitor.read((byte) 0x45);
		monitor.read((byte) 0x46);
		monitor.read((byte) 0x47);
		monitor.read((byte) 0x41);
		monitor.read((byte) 0x42);
		monitor.read((byte) 0x43);
		monitor.readEnd();
	}

	@Test
	public void testEx1() {
		HOProtocol<Object> protocol = new HOProtocol<Object>(
		        new byte[] { (byte) 0x41, (byte) 0x42, (byte) 0x43 },
		        5);
		protocol.addMessageHandler(this);

		ProtocolMonitor<Object> monitor = protocol.createMonitor("abc");
		monitor.read((byte) 0x41);
		monitor.read((byte) 0x42);

		monitor.read((byte) 0x41);
		monitor.read((byte) 0x42);
		monitor.read((byte) 0x43);
		monitor.read((byte) 0x44);
		monitor.read((byte) 0x45);
	}

	@Test
	public void testEx2() {
		HOProtocol<Object> protocol = new HOProtocol<Object>(
		        new byte[] { (byte) 0x41, (byte) 0x42, (byte) 0x43 },
		        8);
		protocol.addMessageHandler(this);

		ProtocolMonitor<Object> monitor = protocol.createMonitor("abc");
		monitor.read((byte) 0x41);
		monitor.read((byte) 0x42);
		monitor.read((byte) 0x43);
		monitor.read((byte) 0x44);
		monitor.read((byte) 0x41);
		monitor.read((byte) 0x42);
		monitor.read((byte) 0x43);
		monitor.read((byte) 0x44);

		monitor.read((byte) 0x45);
		monitor.read((byte) 0x46);
		monitor.read((byte) 0x47);
		monitor.read((byte) 0x48);
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
