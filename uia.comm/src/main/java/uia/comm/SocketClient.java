package uia.comm;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import uia.comm.protocol.Protocol;
import uia.comm.protocol.ProtocolEventArgs;
import uia.comm.protocol.ProtocolEventHandler;
import uia.comm.protocol.ProtocolMonitor;
import uia.utils.ByteUtils;

/**
 * Socket client.
 * 
 * @author Kyle
 * 
 */
public class SocketClient implements ProtocolEventHandler<SocketDataController> {

    private final static Logger logger = Logger.getLogger(SocketClient.class);

    private final Protocol<SocketDataController> protocol;

    private final MessageManager manager;

    private final HashMap<String, MessageCallIn<SocketDataController>> callIns;

    private final HashMap<String, MessageCallOut> callOuts;

    private final String aliasName;

    private final int clientPort;

    private boolean started;

    private SocketDataController controller;

    private SocketChannel ch;

    /**
     * The constructor.
     * 
     * @param protocol The protocol on this socket channel.
     * @param manager Protocol manager.
     * @param aliasName Alias name.
     */
    public SocketClient(
            final Protocol<SocketDataController> protocol,
            final MessageManager manager,
            String aliasName) {
        this(protocol, manager, aliasName, -1);
    }

    /**
     * The constructor.
     * 
     * @param protocol The protocol on this socket channel.
     * @param manager Protocol manager.
     * @param aliasName Alias name.
     * @param clientPort Client port.
     */
    public SocketClient(
            final Protocol<SocketDataController> protocol,
            final MessageManager manager,
            String aliasName,
            int clientPort) {
        this.clientPort = clientPort;
        this.aliasName = aliasName;
        this.protocol = protocol;
        this.protocol.addMessageHandler(this);
        this.manager = manager;
        this.callIns = new HashMap<String, MessageCallIn<SocketDataController>>();
        this.callOuts = new HashMap<String, MessageCallOut>();
        this.started = false;
    }

    /**
     * Get name.
     * 
     * @return The name.
     */
    public String getName() {
        return this.aliasName;
    }

    /**
     * Get protocol on this socket channel.
     * 
     * @return The protocol.
     */
    public Protocol<SocketDataController> getProtocol() {
        return this.protocol;
    }

    /**
     * Register call in worker.
     * 
     * @param callIn The call in worker.
     */
    public void registerCallin(MessageCallIn<SocketDataController> callIn) {
        this.callIns.put(callIn.getCmdName(), callIn);
    }

    /**
     * Connect to specific socket server.
     * 
     * @param address Address.
     * @param port Port no.
     * @return Connect success or not.
     */
    public boolean connect(String address, int port) {
        if (this.started || !SocketClient.ping(address, 2000)) {
            return false;
        }

        try {
            this.ch = SocketChannel.open();
            if (this.clientPort > 0) {
                this.ch.socket().bind(new InetSocketAddress(this.clientPort));
                logger.debug(String.format("%s> connect to %s with port:%d", this.aliasName, address, this.clientPort));
            }

            this.ch.configureBlocking(false);
            this.ch.connect(new InetSocketAddress(InetAddress.getByName(address), port));
            // must have in non-blocking mode!!
            while (!this.ch.finishConnect()) {
            }
            this.controller = new SocketDataController(this.aliasName, this.ch, this.protocol.createMonitor(this.aliasName));
            this.controller.start();

            logger.info(String.format("%s> connect to %s", this.aliasName, address));
            this.started = true;
            return true;
        }
        catch (Exception ex) {
            logger.error(String.format("%s> connect to %s failure. ex:%s", this.aliasName, address, ex.getMessage()));
            this.started = false;
            this.ch = null;
            this.controller = null;
            return false;
        }
    }

    /**
     * Disconnect to socket server.
     */
    public void disconnect() {
        if (!this.started || this.ch == null) {
            return;
        }

        try {
            this.controller.stop();
            this.ch.close();
            logger.info(String.format("%s> disconnect", this.aliasName));
        }
        catch (Exception ex) {

        }
        finally {
            this.controller = null;
            this.ch = null;
        }

    }

    /**
     * Send data to socket server.
     * 
     * @param data Data.
     * @return Send result.
     */
    public boolean send(final byte[] data) {
        return send(data, 1);
    }

    /**
     * Send data to socket server.
     * 
     * @param data Data.
     * @param times Retry times.
     * @return Send result.
     */
    public boolean send(final byte[] data, int times) {
        if (!this.started) {
            return false;
        }

        byte[] encoded = this.manager.encode(data);
        try {
            boolean result = this.controller.send(encoded, times);
            logger.debug(String.format("%s> send %s", this.aliasName, ByteUtils.toHexString(encoded)));
            return result;
        }
        catch (Exception ex) {
            logger.error(String.format("%s> send %s failure. ex:%s",
                    this.aliasName,
                    ByteUtils.toHexString(encoded),
                    ex.getMessage()));
            return false;
        }
    }

    /**
     * Send data to socket server.
     * 
     * @param data Data.
     * @param callOut Reply message worker.
     * @param timeout Timeout seconds.
     * @return Send result.
     */
    public boolean send(final byte[] data, final MessageCallOut callOut, long timeout) {
        if (!this.started) {
            return false;
        }

    	byte[] encoded = this.manager.encode(data);
        if (this.controller.send(encoded, 1)) {
            final String tx = callOut.getTxId();
            synchronized (this.callOuts) {
                this.callOuts.put(tx, callOut);
            }

            logger.debug(String.format("%s> send %s", this.aliasName, ByteUtils.toHexString(encoded)));
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    MessageCallOut out = null;
                    synchronized (SocketClient.this.callOuts) {
                        if (SocketClient.this.callOuts.containsKey(tx)) {
                            logger.debug(String.format("%s> tx:%s callOut timeout", SocketClient.this.aliasName, callOut.getTxId()));
                            out = SocketClient.this.callOuts.remove(tx);
                        }
                    }
                    if (out != null) {
                        try {
                            out.timeout();
                        }
                        catch (Exception ex) {

                        }
                    }
                }

            }, timeout);
            return true;
        }
        else {
            logger.debug(String.format("%s> send %s failure", this.aliasName, ByteUtils.toHexString(encoded)));
            return false;
        }
    }

    @Override
    public void messageReceived(final ProtocolMonitor<SocketDataController> monitor, final ProtocolEventArgs args) {
        final byte[] received = this.manager.decode(args.getData());

        // get command
        String cmd = this.manager.findCmd(received);
        if (cmd == null) {
            logger.debug(String.format("%s> cmd: missing", this.aliasName));
            return;
        }

        if (this.manager.isCallIn(cmd)) {
            final MessageCallIn<SocketDataController> callIn = this.callIns.get(cmd);
            if (callIn == null) {
                logger.debug(String.format("%s> cmd:%s callIn missing", this.aliasName, cmd));
                return;
            }

            logger.debug(String.format("%s> cmd:%s callIn", this.aliasName, cmd));
            new Thread(new Runnable() {

                @Override
                public void run() {
                    callIn.execute(received, SocketClient.this.controller);
                }

            }).start();
        }
        else {
            String tx = this.manager.findTx(received);
            final MessageCallOut callOut = this.callOuts.get(tx);
            if (callOut == null) {
                logger.debug(String.format("%s> cmd:%s tx:%s callout reply missing", this.aliasName, cmd, tx));
                return;
            }

            synchronized (this.callOuts) {
                this.callOuts.remove(tx);
            }

            logger.debug(String.format("%s> cmd:%s tx:%s callout reply", this.aliasName, cmd, tx));
            new Thread(new Runnable() {

                @Override
                public void run() {
                    callOut.execute(received);
                }

            }).start();
        }
    }

    @Override
    public void messageError(final ProtocolMonitor<SocketDataController> monitor, final ProtocolEventArgs args) {
        logger.debug(String.format("%s> %s pack message error",
                this.aliasName,
                monitor.getProtocol().getAliasName()));
        logger.debug("error data: " + ByteUtils.toHexString(args.getData(), "-"));
    }

    private static boolean ping(String ip, int timeout) {
        if (timeout < 0) {
            timeout = 5000;
        }

        boolean alive = false;
        Process p = null;
        String osName = System.getProperty("os.name");
        try {
            Runtime r = Runtime.getRuntime();
            if (osName.indexOf("Windows") >= 0) {
                p = r.exec("ping -w " + timeout + " " + ip);
                if (p == null) {
                    alive = false;
                }
                else {
                    BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    String line = new String();
                    for (int i = 0; i < 7; i++) {
                        line = br.readLine().toLowerCase();
                        if (line.indexOf("ttl") >= 0 && line.indexOf("time") >= 0) {
                            alive = true;
                            break;
                        }
                    }
                    br.close();
                }
            }
            else {
                p = r.exec("ping -c 1 -W " + timeout + " " + ip);
                if (p == null) {
                    alive = false;
                }
                else {
                    alive = p.waitFor() == 0;
                }

            }
        }
        catch (Exception e) {
            alive = false;
        }
        finally {
            if (p != null) {
                p.destroy();
            }
        }
        return alive;
    }
}
