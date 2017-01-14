package uia.comm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
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

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

/**
 *
 * @author Kyle
 *
 */
public class RS232 implements ProtocolEventHandler<RS232>, CommClient<RS232> {

    private final static Logger logger = Logger.getLogger(RS232.class);

    private String aliasName;

    private SerialPort serialPort;

    private final Protocol<RS232> protocol;

    private final MessageManager manager;

    private ProtocolMonitor<RS232> monitor;

    private InputStream in;

    private OutputStream out;

    private final HashMap<String, MessageCallIn<RS232>> callIns;

    private final HashMap<String, MessageCallOut> callOuts;

    private boolean started;

    private int baudrate;

    private int dataBits;

    private int stopBits;

    private int parity;

    public RS232(final Protocol<RS232> protocol, final MessageManager manager, String aliasName) {
        this.protocol = protocol;
        this.protocol.addMessageHandler(this);
        this.manager = manager;
        this.callIns = new HashMap<String, MessageCallIn<RS232>>();
        this.callOuts = new HashMap<String, MessageCallOut>();
        this.started = false;
        this.aliasName = aliasName;
    }

    /**
     * Connect.
     * @param portName RS232 port name.
     * @param baudrate Baud rate.
     * @param dataBits Data bits.
     * @param stopBits Stop bits.
     * @param parity Parity.
     * @return Success or not.
     * @throws Exception
     */
    public boolean connect(String portName, int baudrate, int dataBits, int stopBits, int parity) throws Exception {
        this.baudrate = baudrate;
        this.dataBits = dataBits;
        this.stopBits = stopBits;
        this.parity = parity;

        this.monitor = this.protocol.createMonitor(this.aliasName);
        this.monitor.setController(this);

        if (this.started) {
            return true;
        }

        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
        if (portIdentifier.isCurrentlyOwned()) {
            return false;
        }

        CommPort commPort = portIdentifier.open(getClass().getName(), 2000);

        this.serialPort = (SerialPort) commPort;
        this.serialPort.setSerialPortParams(this.baudrate, this.dataBits, this.stopBits, this.parity);

        this.in = this.serialPort.getInputStream();
        this.out = this.serialPort.getOutputStream();
        this.serialPort.addEventListener(new SerialPortEventListener() {

            @Override
            public void serialEvent(SerialPortEvent evt) {
                messageReceived();
            }
        });
        this.serialPort.notifyOnDataAvailable(true);

        this.started = true;

        return true;
    }

    @Override
    public boolean isConnected() {
        return this.started;
    }

    @Override
    public void disconnect() {
        if (!this.started) {
            return;
        }
        this.serialPort.close();
        this.started = false;
    }

    @Override
    public Protocol<RS232> getProtocol() {
        return this.protocol;
    }

    @Override
    public String getName() {
        return this.aliasName;
    }

    @Override
    public void registerCallin(MessageCallIn<RS232> callIn) {
        this.callIns.put(callIn.getCmdName(), callIn);
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
            final byte[] encoded = this.manager.encode(data);
            while (times > 0) {
                try {
                    this.out.write(encoded);
                    logger.debug(String.format("%s> send %s", this.aliasName, ByteUtils.toHexString(data, 100)));
                    return true;
                }
                catch (Exception ex) {

                }
                finally {
                    times--;
                }
            }
            return false;
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

        MessageCallOutConcurrent callout = new MessageCallOutConcurrent(txId, timeout);
        ExecutorService threadPool = Executors.newSingleThreadExecutor();

        synchronized (this.callOuts) {
            this.callOuts.put(txId, callout);
        }

        try {
            this.out.write(this.manager.encode(data));
            logger.debug(String.format("%s> send %s", this.aliasName, ByteUtils.toHexString(data, 100)));
        }
        catch (Exception ex) {
            logger.debug(String.format("%s> send %s failure", this.aliasName, ByteUtils.toHexString(data, 100)));
            throw new SocketException(this.aliasName + "> send failure");
        }

        try {
            Future<byte[]> future = threadPool.submit(callout);
            return future.get();
        }
        catch (Exception e) {
            logger.error(String.format("%s> callout failed", this.aliasName), e);
            return null;
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

        try {
            this.out.write(this.manager.encode(data));
            logger.debug(String.format("%s> send %s", this.aliasName, ByteUtils.toHexString(data, 100)));
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    MessageCallOut out = null;
                    synchronized (RS232.this.callOuts) {
                        if (RS232.this.callOuts.containsKey(tx)) {
                            logger.debug(String.format("%s> tx:%s callOut timeout", RS232.this.aliasName, callOut.getTxId()));
                            out = RS232.this.callOuts.remove(tx);
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
        catch (Exception ex) {
            synchronized (this.callOuts) {
                this.callOuts.remove(tx);
            }
            logger.debug(String.format("%s> send %s failure", this.aliasName, ByteUtils.toHexString(data, 100)));
            return false;
        }
    }

    @Override
    public void messageReceived(ProtocolMonitor<RS232> monitor, ProtocolEventArgs args) {
        if (args.getData() == null || args.getData().length == 0) {
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
            final MessageCallIn<RS232> callIn = this.callIns.get(cmd);
            if (callIn == null) {
                logger.debug(String.format("%s> cmd:%s callIn missing", this.aliasName, cmd));
                return;
            }

            logger.debug(String.format("%s> cmd:%s callIn", this.aliasName, cmd));
            new Thread(new Runnable() {

                @Override
                public void run() {
                    callIn.execute(received, RS232.this);
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
    public void messageError(ProtocolMonitor<RS232> monitor, ProtocolEventArgs args) {

    }

    private void messageReceived() {
        int data;
        try {
            while ((data = this.in.read()) > -1) {
                if (data == '\n') {
                    break;
                }
                this.monitor.read((byte) data);
            }
            this.monitor.readEnd();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
