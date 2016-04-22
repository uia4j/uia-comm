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
public class SocketClient implements ProtocolEventHandler<SocketDataController>, CommClient<SocketDataController> {

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
     * @param address Address.
     * @param port Port no.
     * @return True if connect success or connected already.
     */
    public synchronized boolean connect(String address, int port) {
        disconnect();

        this.addr = address;
        this.port = port;
        this.started = false;

        return tryConnect();
    }

    /**
     * Try connect to remote.
     * @return Connected or not.
     */
    public synchronized boolean tryConnect() {
        if (this.addr == null) {
            this.started = false;
            return false;
        }

        if (this.started) {
            return true;
        }

        try {
            this.ch = SocketChannel.open();
            if (this.clientPort > 0) {              // with specific port
                this.ch.socket().bind(new InetSocketAddress(this.clientPort));
            }

            // this.ch.configureBlocking(true);     // default is true ?!
            this.ch.socket().connect(new InetSocketAddress(InetAddress.getByName(this.addr), this.port), 2000);
            this.ch.configureBlocking(false);

            /**
            this.ch.configureBlocking(false);
            this.ch.connect(new InetSocketAddress(InetAddress.getByName(this.addr), this.port));
            // must have in non-blocking mode!!
            while (!this.ch.finishConnect()) {
            }
             */

            this.controller = new SocketDataController(
                    this.aliasName,
                    this.ch,
                    this.manager,
                    this.protocol.createMonitor(this.aliasName));
            this.controller.start();

            if (this.clientPort > 0) {
                logger.info(String.format("%s> connect to %s:%s(%d)",
                        this.aliasName,
                        this.addr,
                        this.port,
                        this.clientPort));
            }
            else {
                logger.info(String.format("%s> connect to %s:%s",
                        this.aliasName,
                        this.addr,
                        this.port));
            }
            this.started = true;
            return true;
        }
        catch (Exception ex) {
            if (this.clientPort > 0) {
                logger.error(String.format("%s> connect to %s:%s(%d) failure. %s",
                        this.aliasName,
                        this.addr,
                        this.port,
                        this.clientPort,
                        ex.getMessage()));
            }
            else {
                logger.error(String.format("%s> connect to %s:%s failure. %s",
                        this.aliasName,
                        this.addr,
                        this.port,
                        ex.getMessage()));

            }
            disconnect();
            return false;
        }
    }

    @Override
    public Protocol<SocketDataController> getProtocol() {
        return this.protocol;
    }

    @Override
    public void registerCallin(MessageCallIn<SocketDataController> callIn) {
        this.callIns.put(callIn.getCmdName(), callIn);
    }

    @Override
    public boolean isConnected() {
        return this.started;
    }

    @Override
    public synchronized void disconnect() {
        if (!this.started || this.controller == null) {
            return;
        }

        try {
            this.controller.stop();
            logger.info(String.format("%s> disconnect", this.aliasName));
        }
        catch (Exception ex) {

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
            boolean result = this.controller.send(data, times);
            logger.debug(String.format("%s> send %s", this.aliasName, ByteUtils.toHexString(data, 100)));
            return result;
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
        if (!this.started) {
            throw new SocketException(this.aliasName + "> is not started.");
        }

        MessageCallOutConcurrent callout = new MessageCallOutConcurrent(txId, timeout);
        ExecutorService threadPool = Executors.newSingleThreadExecutor();

        try {
            synchronized (this.callOuts) {
                this.callOuts.put(txId, callout);
            }

            if (this.controller.send(data, 1)) {
                logger.debug(String.format("%s> send %s", this.aliasName, ByteUtils.toHexString(data, 100)));
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
        if (!this.started) {
            throw new SocketException(this.aliasName + "> is not started.");
        }

        final String tx = callOut.getTxId();
        synchronized (this.callOuts) {
            this.callOuts.put(tx, callOut);
        }

        if (this.controller.send(data, 1)) {
            logger.debug(String.format("%s> send %s", this.aliasName, ByteUtils.toHexString(data, 100)));
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
            logger.debug(String.format("%s> send %s failure", this.aliasName, ByteUtils.toHexString(data, 100)));
            return false;
        }
    }

    @Override
    public void messageReceived(final ProtocolMonitor<SocketDataController> monitor, final ProtocolEventArgs args) {
        if (args.getData() == null || args.getData().length == 0) {
            return;
        }

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
                    // TODO: handle close
                    this.controller.receive();
                }
                catch (IOException ex) {

                }
            }
        }
    }
}
