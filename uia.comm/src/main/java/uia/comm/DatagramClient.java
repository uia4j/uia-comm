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

import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.channels.DatagramChannel;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uia.comm.protocol.Protocol;
import uia.comm.protocol.ProtocolEventArgs;
import uia.comm.protocol.ProtocolEventHandler;
import uia.comm.protocol.ProtocolMonitor;
import uia.utils.ByteUtils;

/**
 * UDP client.
 *
 * @author Kyle K. Lin
 *
 */
public class DatagramClient implements ProtocolEventHandler<DatagramDataController>, CommClient<DatagramDataController> {

    private final static Logger logger = LoggerFactory.getLogger(DatagramClient.class);

    private final Protocol<DatagramDataController> protocol;

    private final MessageManager manager;

    private final HashMap<String, MessageCallIn<DatagramDataController>> callIns;

    private final String aliasName;

    private int broadcastPort;

    private boolean started;

    private DatagramDataController controller;

    private DatagramChannel ch;

    private String listenAddress;
    
    private int listenPort;
    
    private int maxCache;

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
        this.started = false;
        this.maxCache = 20 * 1024;  // 20K
    }

    public int getMaxCache() {
        return this.maxCache;
    }

    public void setMaxCache(int maxCache) {
        this.maxCache = Math.max(16, maxCache);
    }

	public int getBroadcastPort() {
		return broadcastPort;
	}

	public void setBroadcastPort(int broadcastPort) {
		this.broadcastPort = broadcastPort;
	}

	public String getListenAddress() {
		return listenAddress;
	}

	public void setListenAddress(String listenAddress) {
		this.listenAddress = listenAddress;
	}

	public int getListenPort() {
		return listenPort;
	}

	public void setListenPort(int listenPort) {
		this.listenPort = listenPort;
	}

	@Override
    public String getName() {
        return this.aliasName;
    }

    public synchronized boolean connect(String listenAddress, int listenPort, int broadcastPort) {
        disconnect();

        this.listenAddress = listenAddress;
        this.listenPort = listenPort;
        this.broadcastPort = broadcastPort;
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
            this.ch = DatagramChannel.open();
            /** Android API 24 above
            this.ch = DatagramChannel.open(StandardProtocolFamily.INET);
            this.ch.setOption(StandardSocketOptions.SO_RCVBUF, 2 * this.maxCache);
            this.ch.setOption(StandardSocketOptions.SO_SNDBUF, 2 * this.maxCache);
             */
            this.ch.socket().setBroadcast(true);
            this.ch.configureBlocking(true);
            this.ch.socket().bind(new InetSocketAddress(this.listenAddress, this.listenPort));
            this.controller = new DatagramDataController(
                    this.aliasName,
                    this.broadcastPort,
                    this.ch,
                    this.manager,
                    this.protocol.createMonitor(this.aliasName));
            this.controller.setMaxCache(this.maxCache);
            this.controller.start();
            this.started = true;
            
            logger.info(String.format("%s> listen on %s:%s, broadcastPort:%s",
                    this.aliasName,
                    this.listenAddress,
                    this.listenPort,
                    this.broadcastPort));
            
            return true;
        }
        catch (Exception ex) {
            logger.error(String.format("%s> listen failed on %s:%s, broadcastPort:%s",
                    this.aliasName,
                    this.listenAddress,
                    this.listenPort,
                    this.broadcastPort), ex);
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
            logger.error(String.format("%s> send %s failure. ex:%s",
                    this.aliasName,
                    ByteUtils.toHexString(data, 100),
                    ex.getMessage()));
            return false;
        }
    }

    @Override
    public byte[] send(final byte[] data, String txId, long timeout) throws SocketException {
    	throw new SocketException("Not support on UDP. Use send(byte[], int).");
    }

    @Override
    public byte[] send(final byte[] data, String txId, long timeout, int retry) throws SocketException {
    	throw new SocketException("Not support on UDP. Use send(byte[], int).");
    }

    @Override
    public boolean send(final byte[] data, final MessageCallOut callOut, long timeout) throws SocketException {
    	throw new SocketException("Not support on UDP. Use send(byte[], int).");
    }

    @Override
    public boolean send(final byte[] data, final MessageCallOut callOut, long timeout, int retry) throws SocketException {
    	throw new SocketException("Not support on UDP. Use send(byte[], int).");
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
            logger.error(String.format("%s> cmd:%s callIn NOT FOUND", this.aliasName, cmd));
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