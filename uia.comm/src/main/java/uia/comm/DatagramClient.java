package uia.comm;

import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.channels.DatagramChannel;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import uia.comm.protocol.Protocol;
import uia.comm.protocol.ProtocolEventArgs;
import uia.comm.protocol.ProtocolEventHandler;
import uia.comm.protocol.ProtocolMonitor;
import uia.utils.ByteUtils;

public class DatagramClient implements ProtocolEventHandler<DatagramDataController>, CommClient<DatagramDataController> {

    private final static Logger logger = Logger.getLogger(DatagramClient.class);

    private final Protocol<DatagramDataController> protocol;

    private final MessageManager manager;

    private final HashMap<String, MessageCallIn<DatagramDataController>> callIns;

    private final HashMap<String, MessageCallOut> callOuts;

    private final String aliasName;

    private boolean started;

    private DatagramDataController controller;

    private DatagramChannel ch;

    private String addr;

    private int port;

    /**
     * The constructor.
     *
     * @param protocol The protocol on this socket channel.
     * @param manager Protocol manager.
     * @param aliasName Alias name.
     */
    public DatagramClient(final Protocol<DatagramDataController> protocol, final MessageManager manager, String aliasName) {
        this.aliasName = aliasName;
        this.protocol = protocol;
        this.protocol.addMessageHandler(this);
        this.manager = manager;
        this.callIns = new HashMap<String, MessageCallIn<DatagramDataController>>();
        this.callOuts = new HashMap<String, MessageCallOut>();
        this.started = false;
    }

    @Override
    public String getName() {
        return this.aliasName;
    }

    /**
     * Get address.
     * @return Address.
     */
    public String getAddr() {
        return this.addr;
    }

    /**
     * Set address.
     * @param addr Address.
     */
    public void setAddr(String addr) {
        this.addr = addr;
    }

    /**
     * Get port no.
     * @return Port no.
     */
    public int getPort() {
        return this.port;
    }

    /**
     * Set port no.
     * @param port Port no.
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Connect to specific socket server.
     *
     * @param port Port no.
     * @return True if connect success or connected already.
     */
    public synchronized boolean connect(int port) {
        disconnect();

        this.addr = null;
        this.port = port;
        this.started = false;

        return tryConnect();
    }

    /**
     * Connect to specific socket server.
     *
     * @param port Port no.
     * @param address Address.
     * @return True if connect success or connected already.
     */
    public synchronized boolean connect(int port, String address) {
        disconnect();

        this.addr = address;
        this.port = port;
        this.started = false;

        return tryConnect();
    }

    public void lastUpdate() {
        this.controller.lastUpdate();
    }

    public boolean isIdle(int timeout) {
        return this.controller.isIdle(timeout);
    }

    /**
     * Try connect to remote.
     * @return Connected or not.
     */
    public synchronized boolean tryConnect() {
        if (this.started) {
            return true;
        }

        try {
        	InetSocketAddress isa = this.addr == null ?
        			new InetSocketAddress(this.port) :
        			new InetSocketAddress(this.addr, this.port);
            this.ch = DatagramChannel.open();
            this.ch.configureBlocking(false);
            this.ch.socket().bind(isa);
            this.controller = new DatagramDataController(
                    this.aliasName,
                    this.ch,
                    isa,
                    this.manager,
                    this.protocol.createMonitor(this.aliasName));

            logger.info(String.format("%s> connect to %s:%s",
                    this.aliasName,
                    this.addr,
                    this.port));

            this.controller.start();
            this.started = true;
            return true;
        }
        catch (Exception ex) {
            logger.error(String.format("%s> connect to %s:%s failure. %s",
                    this.aliasName,
                    this.addr,
                    this.port,
                    ex.getMessage()));

            logger.error(ex);
            disconnect();
            return false;
        }
    }

    @Override
    public Protocol<DatagramDataController> getProtocol() {
        return this.protocol;
    }

    @Override
    public void registerCallin(MessageCallIn<DatagramDataController> callIn) {
        this.callIns.put(callIn.getCmdName(), callIn);
    }

    @Override
    public boolean isConnected() {
        return this.started;
    }

    @Override
    public synchronized void disconnect() {
        if (!this.started || this.controller == null || this.ch == null) {
            return;
        }

        try {
            logger.info(String.format("%s> disconnect", this.aliasName));
        }
        catch (Exception ex) {
            logger.error(String.format("%s> disconnect - %s", this.aliasName, ex));
        }
        finally {
            this.started = false;
            this.controller = null;
            this.ch = null;
            System.gc();
        }

    }

    @Override
    public boolean send(final byte[] data) throws SocketException {
        return send(data, 1);
    }

    @Override
    public boolean send(final byte[] data, int times) throws SocketException {
        if (!this.started) {
            throw new SocketException(this.aliasName + "> is not started.");
        }

        try {
            return this.controller.send(data, times);
        }
        catch (Exception ex) {
            logger.error(String.format("%s> send %s failure. ex:%s",
                    this.aliasName,
                    ByteUtils.toHexString(data, 100),
                    ex.getMessage()));
            return false;
        }
    }

    @Override
    public byte[] send(final byte[] data, String txId, long timeout) throws SocketException {
        return send(data, txId, timeout, 1);
    }

    @Override
    public byte[] send(final byte[] data, String txId, long timeout, int retry) throws SocketException {
        if (!this.started) {
            throw new SocketException(this.aliasName + "> is not started.");
        }

        MessageCallOutConcurrent callout = new MessageCallOutConcurrent(getName(), txId, timeout);
        ExecutorService threadPool = Executors.newSingleThreadExecutor();

        try {
            synchronized (this.callOuts) {
                this.callOuts.put(txId, callout);
            }

            if (this.controller.send(data, retry)) {
                try {
                    Future<byte[]> future = threadPool.submit(callout);
                    return future.get();
                }
                catch (Exception e) {
                    logger.error(String.format("%s> callout failed", this.aliasName), e);
                    return null;
                }
            }
            else {
                logger.debug(String.format("%s> send %s failure", this.aliasName, ByteUtils.toHexString(data, 100)));
                throw new SocketException(this.aliasName + "> send failure");
            }
        }
        finally {
            threadPool.shutdown();
            synchronized (this.callOuts) {
                this.callOuts.remove(txId);
            }
        }
    }

    @Override
    public boolean send(final byte[] data, final MessageCallOut callOut, long timeout) throws SocketException {
        return send(data, callOut, timeout, 1);
    }

    @Override
    public boolean send(final byte[] data, final MessageCallOut callOut, long timeout, int retry) throws SocketException {
        if (!this.started) {
            throw new SocketException(this.aliasName + "> is not started.");
        }

        final String tx = callOut.getTxId();
        synchronized (this.callOuts) {
            this.callOuts.put(tx, callOut);
        }

        if (this.controller.send(data, retry)) {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    MessageCallOut out = null;
                    synchronized (DatagramClient.this.callOuts) {
                        if (DatagramClient.this.callOuts.containsKey(tx)) {
                            logger.debug(String.format("%s> tx:%s callOut timeout", DatagramClient.this.aliasName, callOut.getTxId()));
                            out = DatagramClient.this.callOuts.remove(tx);
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
            synchronized (this.callOuts) {
                this.callOuts.remove(tx);
            }
            logger.debug(String.format("%s> send %s failure", this.aliasName, ByteUtils.toHexString(data, 100)));
            return false;
        }
    }

    @Override
    public void messageReceived(final ProtocolMonitor<DatagramDataController> monitor, final ProtocolEventArgs args) {
        if (args.getData() == null || args.getData().length == 0) {
            return;
        }

        if (monitor.getController() != this.controller) {
            return;
        }

        final byte[] received = this.manager.decode(args.getData());
        if (!this.manager.validate(received)) {
            logger.debug(String.format("%s> data wrong: %s", this.aliasName, ByteUtils.toHexString(received, "-")));
            return;
        }

        // get command
        String cmd = this.manager.findCmd(received);
        if (cmd == null) {
            logger.debug(String.format("%s> cmd: missing", this.aliasName));
            return;
        }

        if (this.manager.isCallIn(cmd)) {
            final MessageCallIn<DatagramDataController> callIn = this.callIns.get(cmd);
            if (callIn == null) {
                logger.debug(String.format("%s> cmd:%s callIn missing", this.aliasName, cmd));
                return;
            }

            logger.debug(String.format("%s> cmd:%s callIn", this.aliasName, cmd));
            new Thread(new Runnable() {

                @Override
                public void run() {
                    callIn.execute(received, DatagramClient.this.controller);
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
    public void messageError(final ProtocolMonitor<DatagramDataController> monitor, final ProtocolEventArgs args) {
        if (monitor.getController() != this.controller) {
            return;
        }

        logger.debug(String.format("%s> %s pack message error",
                this.aliasName,
                monitor.getProtocol().getAliasName()));
        logger.debug("error data: " + ByteUtils.toHexString(args.getData(), "-"));
    }
}