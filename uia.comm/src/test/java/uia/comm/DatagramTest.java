package uia.comm;

import org.junit.Test;

import uia.comm.my.MyManager;
import uia.comm.protocol.ng.NGProtocol;

public class DatagramTest {

    @Test
    public void test() throws Exception {
        DatagramServer server = new DatagramServer(
                new NGProtocol<DatagramDataController>(),
                5678,
                new MyManager(),
                "server");

        server.connect();

        DatagramClient client = new DatagramClient(
                new NGProtocol<DatagramDataController>(),
                new MyManager(),
                "client");

        client.connect("localhost", 5678);
        Thread.sleep(2000);
        client.send("12345".getBytes());
        Thread.sleep(2000);
        client.send("ABCDE".getBytes());
        Thread.sleep(2000);
        client.send("1234567890abcdefg".getBytes());
        Thread.sleep(2000);
        client.disconnect();
        System.out.println("client closed");

        Thread.sleep(2000);
        server.disconnect();
        System.out.println("server closed");
    }
}
