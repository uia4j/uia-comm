package uia.comm;

import java.net.SocketException;

import org.junit.Test;

import uia.comm.protocol.ht.HTProtocol;
import uia.utils.ByteUtils;
import uia.utils.HexStringUtils;

public class DatagramTest implements MessageManager {

    @Test
    public void testClient() throws Exception {
        final byte[] data1 = HexStringUtils.toBytes("41-42-00-00-00-41-42-43-30-31-32-33-34-35-36-37-38-39-99-99-99", "-");
        final byte[] data2 = HexStringUtils.toBytes("41-42-00-00-00-41-42-43-31-32-33-34-35-36-76-38-39-30-99-99-99", "-");

        // client1:10002 <-----> client2:10003
        // client1
        final DatagramClient client1 = new DatagramClient(
                new HTProtocol<DatagramDataController>(
                		new byte[] { 0x00, 0x00, 0x00 }, 
                		new byte[] { (byte)0x99, (byte)0x99, (byte)0x99 }),
                this,
                "client");
        client1.connect("localhost", 10002, 10003);
        client1.registerCallin(new MessageCallIn<DatagramDataController>() {

			@Override
			public String getCmdName() {
				return "ABC";
			}

			@Override
			public void execute(byte[] request,	DatagramDataController controller) {
				System.out.println("1> rcv:" + new String(ByteUtils.copy(request, 3, 13)));
				
			}
        	
        });

        // client2
        final DatagramClient client2 = new DatagramClient(
                new HTProtocol<DatagramDataController>(
                		new byte[] { 0x00, 0x00, 0x00 }, 
                		new byte[] { (byte)0x99, (byte)0x99, (byte)0x99 }),
                this,
                "client");
        client2.connect("localhost", 10003, 10002);
        client2.registerCallin(new MessageCallIn<DatagramDataController>() {

			@Override
			public String getCmdName() {
				return "ABC";
			}

			@Override
			public void execute(byte[] request,	DatagramDataController controller) {
				System.out.println("2> rcv:" + new String(ByteUtils.copy(request, 3, 13)));
				try {
					client2.send(data2);
				} catch (SocketException e) {
					e.printStackTrace();
				}
				
			}
        	
        });
        
        Thread.sleep(1000);
        client1.send(data1);  
        Thread.sleep(50);
        client1.send(data1);  
        client1.send(data1);  
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
