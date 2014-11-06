package uia.comm.protocol;

import org.junit.Test;

import uia.comm.protocol.hl.HLProtocol;
import uia.utils.ByteUtils;

public class HLProtocolTest implements ProtocolEventHandler<Object> {

	private final HLProtocol<Object> protocol;

	public HLProtocolTest() {
		this.protocol = new HLProtocol<Object>(
		        5,
		        1,
		        3,
		        2,
		        new LenReader() {

			        @Override
			        public int read(byte[] data) {
				        int len = data[0] << 8;
				        len += data[1];
				        return len;
			        }
		        },
		        new byte[] { 0x10, 0x01 });
		this.protocol.addMessageHandler(this);
	}

	@Test
	public void testNormal1() {
		String sample = "10-01-01-00-08-22-07-de-06-0a-00-1c-34-c7-10-01-01";
		ProtocolMonitor<Object> monitor = this.protocol.createMonitor("abc");
		monitor.read((byte) 0x10);
		monitor.read((byte) 0x01);
		monitor.read((byte) 0x01);
		monitor.read((byte) 0x00);
		monitor.read((byte) 0x08);
		monitor.read((byte) 0x22);
		monitor.read((byte) 0x07);
		monitor.read((byte) 0xde);
		monitor.read((byte) 0x06);
		monitor.read((byte) 0x0a);
		monitor.read((byte) 0x00);
		monitor.read((byte) 0x1c);
		monitor.read((byte) 0x34);
		monitor.read((byte) 0xc7);
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
