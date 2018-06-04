package uia.comm;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import uia.comm.SocketServer.ConnectionStyle;
import uia.comm.protocol.Protocol;
import uia.comm.protocol.ProtocolEventArgs;
import uia.comm.protocol.ProtocolEventHandler;
import uia.comm.protocol.ProtocolMonitor;
import uia.utils.ByteUtils;

public class DatagramServer implements ProtocolEventHandler<DatagramDataController> {

    private final static Logger logger = Logger.getLogger(DatagramServer.class);

    private final int port;

    private final Protocol<DatagramDataController> protocol;

    private final MessageManager manager;

    private final TreeMap<String, MessageCallIn<DatagramDataController>> callIns;

    private final TreeMap<String, TreeMap<String, MessageCallOut>> clientCallouts;

    private final TreeMap<String, DatagramDataController> controllers;

    private final String aliasName;

    private boolean started;

    private Selector serverSelector;

    private DatagramChannel channel;

    private int idleTime;

    public DatagramServer(Protocol<DatagramDataController> protocol, int port, MessageManager manager, String aliasName) throws Exception {
        this(protocol, port, manager, aliasName, ConnectionStyle.NORMAL);
    }

    /**
     * Constructor.
     *
     * @param protocol The protocol on this socket channel.
     * @param port Socket port.
     * @param manager Protocol manager.
     * @param aliasName Alias name.
     * @throws Exception Raise construct failure.
     */
    public DatagramServer(Protocol<DatagramDataController> protocol, int port, MessageManager manager, String aliasName, ConnectionStyle connectionStyle) throws Exception {
        this.aliasName = aliasName;
        this.protocol = protocol;
        this.protocol.addMessageHandler(this);
        this.manager = manager;
        this.callIns = new TreeMap<String, MessageCallIn<DatagramDataController>>();
        this.clientCallouts = new TreeMap<String, TreeMap<String, MessageCallOut>>();
        this.started = false;
        this.controllers = new TreeMap<String, DatagramDataController>();

        this.idleTime = 60000;
        this.port = port;
    }

    public void connect() throws IOException {
        this.serverSelector = Selector.open();

        this.channel = DatagramChannel.open();
        this.channel.socket().bind(new InetSocketAddress(this.port));
        this.channel.configureBlocking(false);
        this.channel.register(this.serverSelector, SelectionKey.OP_READ);   // Connection-Less

        this.started = true;
        new Thread(new Runnable() {

            @Override
            public void run() {
                running();
            }

        }).start();
    }

    public void disconnect() throws IOException {
        this.started = false;
        this.channel.close();
    }

    @Override
    public synchronized void messageReceived(final ProtocolMonitor<DatagramDataController> monitor, ProtocolEventArgs args) {
        if (args.getData() == null || args.getData().length == 0) {
            return;
        }

        if (this.controllers.get(monitor.getController().getName()) == null) {
            return;
        }

        final byte[] received = this.manager.decode(args.getData());
        System.out.println(new String(received));
        if (!this.manager.validate(received)) {
            logger.debug(String.format("%s> data wrong: %s", this.aliasName, ByteUtils.toHexString(received, "-")));
            return;
        }

        // get command
        String cmd = this.manager.findCmd(received);
        if (cmd == null) {
            logger.debug(String.format("%s> %s> %s cmd:%s missing",
                    this.aliasName,
                    monitor.getName(),
                    monitor.getProtocol().getAliasName(),
                    cmd));
            return;
        }

        monitor.getController().lastUpdate();

        if (this.manager.isCallIn(cmd)) {
            final MessageCallIn<DatagramDataController> callIn = this.callIns.get(cmd);
            if (callIn == null) {
                logger.debug(String.format("%s> %s> %s cmd:%s callIn missing",
                        this.aliasName,
                        monitor.getName(),
                        monitor.getProtocol().getAliasName(),
                        cmd));
                return;
            }

            logger.debug(String.format("%s> %s> %s cmd:%s callIn",
                    this.aliasName,
                    monitor.getName(),
                    monitor.getProtocol().getAliasName(),
                    cmd));
            new Thread(new Runnable() {

                @Override
                public void run() {
                    callIn.execute(received, monitor.getController());
                }

            }).start();
        }
        else {
            TreeMap<String, MessageCallOut> callOuts = this.clientCallouts.get(monitor.getController().getName());
            if (callOuts == null) {
                logger.debug(String.format("%s> %s> not found",
                        this.aliasName,
                        monitor.getController().getName()));
                return;
            }

            String tx = this.manager.findTx(received);
            final MessageCallOut callOut = callOuts.get(tx);
            if (callOut == null) {
                logger.debug(String.format("%s> %s> %s cmd:%s tx:%s callOut reply missing",
                        this.aliasName,
                        monitor.getController().getName(),
                        monitor.getProtocol().getAliasName(),
                        cmd,
                        tx));
                return;
            }

            synchronized (callOuts) {
                callOuts.remove(tx);
            }

            logger.debug(String.format("%s> %s> %s cmd:%s tx:%s callOut reply",
                    this.aliasName,
                    monitor.getController().getName(),
                    monitor.getProtocol().getAliasName(),
                    cmd,
                    tx));
            new Thread(new Runnable() {

                @Override
                public void run() {
                    callOut.execute(received);
                }

            }).start();
        }
    }

    @Override
    public void messageError(ProtocolMonitor<DatagramDataController> monitor, ProtocolEventArgs args) {
        if (args.getData() == null || args.getData().length == 0) {
            return;
        }

        logger.debug(String.format("%s> %s> %s pack message error: %s(%s).",
                this.aliasName,
                monitor.getName(),
                monitor.getProtocol().getAliasName(),
                args.getErrorCode(),
                monitor.getClass().getName()));
        logger.debug(ByteUtils.toHexString(args.getData(), "-"));
    }

    private void running() {
        while (this.started) {
            try {
                if (this.serverSelector.select(1000) == 0) {
                    System.out.println(".");
                    continue;
                }

                Iterator<SelectionKey> keyIter = this.serverSelector.selectedKeys().iterator();
                while (keyIter.hasNext()) {
                    SelectionKey key = keyIter.next();
                    keyIter.remove();

                    DatagramChannel channel = (DatagramChannel) key.channel();
                    if (key.isReadable()) {
                        boolean end = false;
                        while (!end) {
                            ByteBuffer bf = ByteBuffer.allocate(9);
                            SocketAddress address = channel.receive(bf);
                            if (address == null) {
                                end = true;
                                break;
                            }

                            String id = address.toString();
                            if (bf.hasArray()) {
                                DatagramDataController controller = this.controllers.get(id);
                                if (controller == null) {
                                    System.out.println(id + " connected");

                                    controller = new DatagramDataController(id, channel, this.manager, this.protocol.createMonitor(id));
                                    this.controllers.put(id, controller);
                                }
                                int len = bf.position();
                                if (len == 0) {
                                    System.out.println("end1");
                                    end = true;
                                    break;
                                }
                                byte[] temp = bf.array();
                                controller.receive(ByteUtils.copy(temp, 0, len));
                            }
                            bf.clear();
                        }
                    }
                }
            }
            catch (Exception ex) {

            }
        }
    }
}
