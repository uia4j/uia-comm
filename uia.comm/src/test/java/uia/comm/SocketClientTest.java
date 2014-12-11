package uia.comm;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Ignore;
import org.junit.Test;

import uia.comm.my.MyManager;
import uia.comm.protocol.ng.NGProtocol;

public class SocketClientTest {

    public static Logger logger = Logger.getLogger(SocketClientTest.class);

    private final SocketClient socketClient;

    public SocketClientTest() throws Exception {
        PropertyConfigurator.configure("log4j.properties");
        this.socketClient = new SocketClient(
                new NGProtocol<SocketDataController>(),
                new MyManager(),
                "CLIENT");
    }

    @Test
    public void testPing() {
        System.out.println(SocketClient.ping("192.168.0.100", 2000));
    }

    @Test
    @Ignore
    public void testConnectPMC() throws Exception {
        long t1 = System.currentTimeMillis();
        System.out.println(this.socketClient.connect("192.168.0.100", 16000));
        long t2 = System.currentTimeMillis();
        System.out.println(t2 - t1);
        this.socketClient.disconnect();
    }

    @Test
    @Ignore
    public void testConnectOnb() throws Exception {
        long t1 = System.currentTimeMillis();
        System.out.println(this.socketClient.connect("222.66.141.10", 6055));
        long t2 = System.currentTimeMillis();
        System.out.println(t2 - t1);
        Thread.sleep(5000);
        this.socketClient.disconnect();
    }
}
