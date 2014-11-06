package uia.comm.protocol;

import org.junit.Test;

import uia.comm.protocol.ng.NGProtocol;
import uia.utils.ByteUtils;

public class NGProtocolTest implements ProtocolEventHandler<Object> {

	private final NGProtocol<Object> protocol;

	public NGProtocolTest() {
		this.protocol = new NGProtocol<Object>();
		this.protocol.addMessageHandler(this);
	}

	@Test
	public void testNormal1() {
		ProtocolMonitor<Object> monitor = this.protocol.createMonitor("abc");
		monitor.read((byte) 0x43);
		monitor.read((byte) 0x44);
		monitor.read((byte) 0x45);
		monitor.readEnd();
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
