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
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Arrays;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uia.comm.protocol.ProtocolMonitor;
import uia.utils.ByteUtils;

/**
 *
 * @author Kyle K. Lin
 *
 */
public class DatagramDataController implements DataController {

    private final static Logger logger = LoggerFactory.getLogger(DatagramDataController.class);

    private final ProtocolMonitor<DatagramDataController> monitor;

    private final String name;;
    
    private final int broadcastPort;

    private MessageManager mgr;

    private DatagramChannel ch;

    private long lastUpdate;

    private boolean started;

    private Selector selector;

    private int maxCache;

    /**
     *
     * @param name Name.
     * @param ch Datagram channel used to receive and send message.
     * @param monitor Monitor used to handle received message.
     * @param idlePeriod
     * @throws IOException
     */
    DatagramDataController(String name, int broadcastPort, DatagramChannel ch, MessageManager mgr, ProtocolMonitor<DatagramDataController> monitor) throws IOException {
        this.name = name;
        this.broadcastPort = broadcastPort;
        this.ch = ch;
        this.ch.configureBlocking(false);
        this.mgr = mgr;
        this.monitor = monitor;
        this.monitor.setController(this);
        this.lastUpdate = System.currentTimeMillis();
        this.started = false;
        this.maxCache = 8 * 1024;  // 8K
    }

    public int getMaxCache() {
        return this.maxCache;
    }

    public void setMaxCache(int maxCache) {
        this.maxCache = Math.min(2000000, Math.max(16, maxCache));  // 2M
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public synchronized boolean send(byte[] data, int times) {
        this.lastUpdate = System.currentTimeMillis();
        final byte[] encoded = this.mgr.encode(data);
        int _times = Math.max(1, times);
        while (_times > 0) {
            try {
                this.ch.socket().setSendBufferSize(encoded.length);
                int cnt = this.ch.send(ByteBuffer.wrap(encoded), new InetSocketAddress("255.255.255.255", this.broadcastPort));
                if (cnt == encoded.length) {
                    logger.debug(String.format("%s> send %s", this.name, ByteUtils.toHexString(encoded, 100)));
                    return true;
                }
                else {
                    logger.error(String.format("%s> write count error!!", this.name));
                }
            }
            catch (Exception ex) {
            	ex.printStackTrace();
            }
            finally {
                _times--;
            }
        }
        return false;
    }
    
    synchronized boolean start() {
        if (this.ch == null || this.started) {
            return false;
        }

        this.lastUpdate = System.currentTimeMillis();
        try {
            this.selector = Selector.open();
            this.ch.socket().setReuseAddress(true);
            this.ch.register(this.selector, SelectionKey.OP_READ);
        }
        catch (Exception ex) {
            return false;
        }

        this.started = true;
        new Thread(new Runnable() {

            @Override
            public void run() {
                running();
            }

        }).start();
        return true;
    }

    synchronized void stop() {
        if (this.ch != null) {
            try {
                if (this.selector != null) {
                    this.ch.keyFor(this.selector).cancel();
                    this.selector.close();
                }
                this.ch.close();
            }
            catch (Exception ex) {

            }
        }
        this.ch = null;
        this.started = false;
    }

    void lastUpdate() {
        this.lastUpdate = System.currentTimeMillis();
    }

    boolean isIdle(int timeout) {
        return System.currentTimeMillis() - this.lastUpdate > timeout;
    }

    synchronized void receive(byte[] data) throws IOException {
        for (byte b : data) {
            this.monitor.read(b);
        }
        this.monitor.readEnd();
    }

    DatagramChannel getChannel() {
        return this.ch;
    }

    /**
     * Receive message from socket channel.
     *
     * @throws IOException
     */
    synchronized boolean receive() throws IOException {
        if (this.ch == null) {
            logger.debug(this.name + "> no channel");
            return false;
        }

        ByteBuffer buffer = ByteBuffer.allocate(this.maxCache * 2);
        this.ch.receive(buffer);
        int len = buffer.position();
        while (len > 0) {
            logger.debug(this.name + "> is receiving: " + len);
            byte[] value = (byte[]) buffer.flip().array();
            value = Arrays.copyOf(value, len);
            for (byte b : value) {
                if (this.monitor.getDataLength() > this.maxCache) {
                    logger.error(this.name + "> out of maxCchte:" + this.maxCache);
                    this.monitor.reset();
                }
                this.monitor.read(b);
            }
            buffer.clear();
            
            this.ch.receive(buffer);
            len = buffer.position();
        }
        this.monitor.readEnd();
        return true;
    }

    private void running() {
        // use internal selector to handle received data.
        while (this.started) {
            try {
                this.selector.select(); // wait NIO event
            }
            catch (Exception ex) {
                continue;
            }

            if (this.selector.isOpen()) {
                Iterator<SelectionKey> iterator = this.selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey selectionKey = iterator.next();
                    DatagramChannel dgChannel = (DatagramChannel) selectionKey.channel();
                    iterator.remove();

                    if (!selectionKey.isValid()) {
                        continue;
                    }   
                    
                    if (selectionKey.isReadable()) {
                        try {
                            receive();
                        }
                        catch (Exception e) {
                        	logger.error(dgChannel.toString() + " failed", e);
                        	break;
                        }
                    }
                }
            }
        }
    }
}
