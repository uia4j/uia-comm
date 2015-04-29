/*******************************************************************************
 * * Copyright (c) 2015, UIA * All rights reserved. * Redistribution and use in source and binary forms, with or without * modification, are permitted provided that the following conditions are met: * * * Redistributions of source code must retain
 * the above copyright * notice, this list of conditions and the following disclaimer. * * Redistributions in binary form must reproduce the above copyright * notice, this list of conditions and the following disclaimer in the * documentation and/or
 * other materials provided with the distribution. * * Neither the name of the {company name} nor the * names of its contributors may be used to endorse or promote products * derived from this software without specific prior written permission. * *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS "AS IS" AND ANY * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE * DISCLAIMED. IN NO
 * EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; * LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS * SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package uia.comm;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
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

/**
 * Socket client.
 * 
 * @author Kyle
 * 
 */
public class SocketClient implements ProtocolEventHandler<SocketDataController>, SocketApp {

    private final static Logger logger = Logger.getLogger(SocketClient.class);

    private final Protocol<SocketDataController> protocol;

    private final MessageManager manager;

    private final HashMap<String, MessageCallIn<SocketDataController>> callIns;

    private final HashMap<String, MessageCallOut> callOuts;

    private final String aliasName;

    private final int clientPort;

    private boolean started;

    private SocketDataController controller;

    private Selector selector;

    private SocketChannel ch;

    private String addr;

    private int port;

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
     * Is connected or not.
     * @return Connect or not.
     */
    public boolean isConnected() {
        return this.started;
    }

    /**
     * Connect to specific socket server.
     * 
     * @param address Address.
     * @param port Port no.
     * @return True if connect success or connected already.
     */
    public boolean connect(String address, int port) {
        if (this.started) {
            return true;
        }

        // keep address & port
        this.addr = address;
        this.port = port;

        try {
            this.ch = SocketChannel.open();
            if (this.clientPort > 0) {
                this.ch.socket().bind(new InetSocketAddress(this.clientPort));
                logger.debug(String.format("%s> connect to %s with port:%d", this.aliasName, address, this.clientPort));
            }

            this.ch.configureBlocking(true);
            this.ch.socket().connect(new InetSocketAddress(InetAddress.getByName(address), port), 1000);
            this.ch.configureBlocking(false);

            /**
            this.ch.configureBlocking(false);
            this.ch.connect(new InetSocketAddress(InetAddress.getByName(address), port));
            // must have in non-blocking mode!!
            while (!this.ch.finishConnect()) {
            }
            */
            this.controller = new SocketDataController(
                    this.aliasName,
                    this,
                    this.ch,
                    this.protocol.createMonitor(this.aliasName),
                    0);
            this.controller.start();

            logger.info(String.format("%s> connect to %s", this.aliasName, address));
            this.started = true;
            return true;
        }
        catch (Exception ex) {
            logger.error(String.format("%s> connect to %s failure.", this.aliasName, address));
            this.started = false;
            this.ch = null;
            this.controller = null;
            return false;
        }
    }

    /**
     * Try connect to remote.
     * @return Connected or not.
     */
    public boolean tryConnect() {
        if (this.addr == null) {
            return false;
        }

        return connect(this.addr, this.port);
    }

    /**
     * Disconnect to socket server. This will clear address and port information.
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
            this.started = false;
            this.controller = null;
            this.ch = null;
        }

    }

    /**
     * Send data to socket server.
     * 
     * @param data Data.
     * @return Send result.
     * @throws SocketException Raise when server is not connected.
     */
    public boolean send(final byte[] data) throws SocketException {
        return send(data, 1);
    }

    /**
     * Send data to socket server.
     * 
     * @param data Data.
     * @param times Retry times.
     * @return Send result.
     * @throws SocketException Raise when server is not connected.
     */
    public boolean send(final byte[] data, int times) throws SocketException {
        if (!this.started) {
            throw new SocketException(this.aliasName + "> is not started.");
        }

        byte[] encoded = this.manager.encode(data);
        try {
            boolean result = this.controller.send(encoded, times);
            logger.debug(String.format("%s> send %s", this.aliasName, ByteUtils.toHexString(encoded, 100)));
            return result;
        }
        catch (Exception ex) {
            logger.error(String.format("%s> send %s failure. ex:%s",
                    this.aliasName,
                    ByteUtils.toHexString(encoded, 100),
                    ex.getMessage()));
            return false;
        }
    }

    /**
     * send data to socket server and wait result.
     * 
     * @param data Data.
     * @param txId Transaction id.
     * @param timeout Timeout milliseconds.
     * @return Reply data or Null if timeout.
     * @throws SocketException Raise when server is not connected or send to server failure.
     */
    public byte[] send(final byte[] data, String txId, long timeout) throws SocketException {
        if (!this.started) {
            throw new SocketException(this.aliasName + "> is not started.");
        }

        byte[] encoded = this.manager.encode(data);
        MessageCallOutConcurrent callout = new MessageCallOutConcurrent(txId, timeout);
        ExecutorService threadPool = Executors.newSingleThreadExecutor();

        try {
            synchronized (this.callOuts) {
                this.callOuts.put(txId, callout);
            }

            if (this.controller.send(encoded, 1)) {
                logger.debug(String.format("%s> send %s", this.aliasName, ByteUtils.toHexString(encoded, 100)));
                try {
                    Future<byte[]> future = threadPool.submit(callout);
                    return future.get();
                }
                catch (Exception e) {
                    return null;
                }
            }
            else {
                logger.debug(String.format("%s> send %s failure", this.aliasName, ByteUtils.toHexString(encoded, 100)));
                throw new SocketException(this.aliasName + "> send failure");
            }
        }
        finally {
            synchronized (this.callOuts) {
                this.callOuts.remove(txId);
            }
        }
    }

    /**
     * Send data to socket server.
     * 
     * @param data Data.
     * @param callOut Reply message worker.
     * @param timeout Timeout seconds.
     * @return Send result.
     * @throws SocketException Raise if not started.
     */
    public boolean send(final byte[] data, final MessageCallOut callOut, long timeout) throws SocketException {
        if (!this.started) {
            throw new SocketException(this.aliasName + "> is not started.");
        }

        byte[] encoded = this.manager.encode(data);

        final String tx = callOut.getTxId();
        synchronized (this.callOuts) {
            this.callOuts.put(tx, callOut);
        }

        if (this.controller.send(encoded, 1)) {
            logger.debug(String.format("%s> send %s", this.aliasName, ByteUtils.toHexString(encoded, 100)));
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
            synchronized (this.callOuts) {
                this.callOuts.remove(tx);
            }
            logger.debug(String.format("%s> send %s failure", this.aliasName, ByteUtils.toHexString(encoded, 100)));
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

            logger.info(String.format("%s> cmd:%s callIn", this.aliasName, cmd));
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

            logger.info(String.format("%s> cmd:%s tx:%s callout reply", this.aliasName, cmd, tx));
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

    @Override
    public void idle(SocketDataController controller) {
        // DO Nothing
    }

    @SuppressWarnings("unused")
    private void running() {
        // use internal selector to handle received data.
        while (this.started) {
            try {
                this.selector.select(); // wait NIO event
            }
            catch (Exception ex) {
                continue;
            }

            Iterator<SelectionKey> iterator = this.selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();
                SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                iterator.remove();

                try {
                    this.controller.receive();
                }
                catch (IOException e) {

                }
            }
        }
    }

    public static boolean ping(String ip, int timeout) {
        if (timeout < 0) {
            timeout = 3000;
        }

        try {
            InetAddress address = InetAddress.getByName(ip);
            return address.isReachable(3000);
        }
        catch (Exception e) {
            return false;
        }
    }
}
