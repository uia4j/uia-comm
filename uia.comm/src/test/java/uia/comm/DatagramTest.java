package uia.comm;

import org.junit.Test;

import uia.comm.protocol.ht.HTProtocol;
import uia.utils.ByteUtils;
import uia.utils.HexStringUtils;

public class DatagramTest implements MessageManager {

    @Test
    public void testClient() throws Exception {
        byte[] data = HexStringUtils.toBytes("41-42-00-00-00-41-42-43-30-31-32-33-34-35-36-37-38-39-99-99-99", "-");
        DatagramClient client = new DatagramClient(
                new HTProtocol<DatagramDataController>(
                		new byte[] { 0x00, 0x00, 0x00 }, 
                		new byte[] { (byte)0x99, (byte)0x99, (byte)0x99 }),
                this,
                "client");
        client.connect("localhost", 10002, 10002);
        client.registerCallin(new MessageCallIn<DatagramDataController>() {

			@Override
			public String getCmdName() {
				return "ABC";
			}

			@Override
			public void execute(byte[] request,	DatagramDataController controller) {
				System.out.println("rcv:" + new String(ByteUtils.copy(request, 3, 13)));
				
			}
        	
        });
        Thread.sleep(1000);
        client.send(data);  
        Thread.sleep(50);
        client.send(data);  
        client.send(data);  
        Thread.sleep(1000);
        System.out.println("client closed");
    }

	@Override
	public boolean isCallIn(String cmd) {
		return "ABC".equalsIgnoreCase(cmd);
	}

	@Override
	public String findCmd(byte[] data) {
		return new String(new byte[] { data[3], data[4], data[5] });
	}

	@Override
	public String findTx(byte[] data) {
		return "1";
	}

	@Override
	public byte[] decode(byte[] data) {
		return data;
	}

	@Override
	public byte[] encode(byte[] data) {
		return data;
	}

	@Override
	public boolean validate(byte[] data) {
		return true;
	}
}
