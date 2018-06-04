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
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

import org.apache.log4j.Logger;

import uia.comm.protocol.ProtocolMonitor;
import uia.utils.ByteUtils;

/**
 *
 * @author Kyle K. Lin
 *
 */
public class DatagramDataController implements DataController {

    private final static Logger logger = Logger.getLogger(DatagramDataController.class);

    private final ProtocolMonitor<DatagramDataController> monitor;

    private final String name;;

    private MessageManager mgr;

    private DatagramChannel ch;

    private long lastUpdate;

    /**
     *
     * @param name Name.
     * @param ch Datagram channel used to receive and send message.
     * @param monitor Monitor used to handle received message.
     * @param idlePeriod
     * @throws IOException
     */
    DatagramDataController(String name, DatagramChannel ch, MessageManager mgr, ProtocolMonitor<DatagramDataController> monitor) throws IOException {
        this.name = name;
        this.ch = ch;
        this.ch.configureBlocking(false);
        this.mgr = mgr;
        this.monitor = monitor;
        this.monitor.setController(this);
        this.lastUpdate = System.currentTimeMillis();
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
                int cnt = this.ch.write(ByteBuffer.wrap(encoded));
                if (cnt == encoded.length) {
                    logger.debug(String.format("%s> send %s", this.name, ByteUtils.toHexString(encoded, 100)));
                    return true;
                }
                else {
                    logger.fatal(String.format("%s> write count error!!", this.name));
                }
            }
            catch (Exception ex) {

            }
            finally {
                _times--;
            }
        }
        return false;
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
}
