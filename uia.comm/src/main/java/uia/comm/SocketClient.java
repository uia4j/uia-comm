/*******************************************************************************
 * Copyright 2017 UIA
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import java.util.concurrent.ConcurrentHashMap;
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
 * @author Kyle K. Lin
 *
 */
public class SocketClient implements ProtocolEventHandler<SocketDataController>, CommClient<SocketDataController> {

    private final static Logger logger = Logger.getLogger(SocketClient.class);

    private final Protocol<SocketDataController> protocol;

    private final MessageManager manager;

    private final HashMap<String, MessageCallIn<SocketDataController>> callIns;

    private final ConcurrentHashMap<String, MessageCallOut> callOuts;

    private final String aliasName;

    private final int clientPort;

    private boolean started;

    private SocketDataController controller;

    private Selector selector;

    private SocketChannel ch;

    private String addr;

    private int port;

    private int maxCache;

    /**
     * The constructor.
     *
     * @param protocol The protocol on this socket channel.
     * @param manager Protocol manager.
     * @param aliasName Alias name.
     */
    public SocketClient(final Protocol<SocketDataController> protocol, final MessageManager manager, String aliasName) {
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
    public SocketClient(final Protocol<SocketDataController> protocol, final MessageManager manager, String aliasName, int clientPort) {
        this.protocol = protocol;
        this.protocol.addMessageHandler(this);
        this.manager = manager;
        this.callIns = new HashMap<String, MessageCallIn<SocketDataController>>();
        this.callOuts = new ConcurrentHashMap<String, MessageCallOut>();
        this.started = false;
        this.aliasName = aliasName;
        this.maxCache = 20 * 1024;  // 20K
        this.clientPort = clientPort;
    }

    public int getMaxCache() {
        return this.maxCache;
    }

    public void setMaxCache(int maxCache) {
        this.maxCache = Math.max(16, maxCache);
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
        if (this.addr == null) {
            this.started = false;
            return false;
        }

        if (this.started) {
            return true;
        }

        try {
            this.ch = SocketChannel.open();
            if (this.ch == null || this.ch.socket() == null) {
                logger.info(String.format("%s> channel to %s:%s can't be opened",
                        this.aliasName,
                        this.addr,
                        this.port));
                return false;
            }

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
            this.controller.setMaxCache(this.maxCache);
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
                logger.error(String.format("%s> connect to %s:%s(%d) failed. %s",
                        this.aliasName,
                        this.addr,
                        this.port,
                        this.clientPort,
                        ex.getMessage()));
            }
            else {
                logger.error(String.format("%s> connect to %s:%s failed. %s",
                        this.aliasName,
                        this.addr,
                        this.port,
                        ex.getMessage()));

            }
            logger.error(ex);
            disconnect();
            return false;
        }
    }

    @Override
    public String getName() {
        return this.aliasName;
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
        if (!this.started || this.controller == null || this.ch == null) {
            return;
        }

        try {
            this.controller.stop();
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
            logger.error(String.format("%s> send %s failed",
                    this.aliasName,
                    ByteUtils.toHexString(data, 100)),
                    ex);
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

        try {
            this.callOuts.put(txId, callout);
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
                logger.debug(String.format("%s> send %s failed", this.aliasName, ByteUtils.toHexString(data, 100)));
                throw new SocketException(this.aliasName + "> send failed");
            }
        }
        finally {
            threadPool.shutdown();
            this.callOuts.remove(txId);
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
        this.callOuts.put(tx, callOut);

        if (this.controller.send(data, retry)) {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    MessageCallOut out = SocketClient.this.callOuts.remove(tx);
                    if(out != null) {
                        try {
                            logger.info(String.format("%s> tx:%s callOut timeout", SocketClient.this.aliasName, out.getTxId()));
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
            this.callOuts.remove(tx);
            logger.debug(String.format("%s> send %s failed", this.aliasName, ByteUtils.toHexString(data, 100)));
            return false;
        }
    }

    @Override
    public void messageReceived(final ProtocolMonitor<SocketDataController> monitor, final ProtocolEventArgs args) {
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
            final MessageCallOut callOut = this.callOuts.remove(tx);
            if (callOut == null) {
                logger.debug(String.format("%s> cmd:%s tx:%s callout reply missing", this.aliasName, cmd, tx));
                return;
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
        if (monitor.getController() != this.controller) {
            return;
        }

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
