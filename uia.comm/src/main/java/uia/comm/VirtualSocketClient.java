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

import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uia.utils.ByteUtils;

/**
 *
 * @author Kyle K. Lin
 *
 */
public class VirtualSocketClient {

    private final static Logger logger = LoggerFactory.getLogger(VirtualSocketClient.class);

    private SocketClient activeClient;

    private LinkedBlockingQueue<SocketClient> queues;

    public VirtualSocketClient() {
        this.queues = new LinkedBlockingQueue<SocketClient>();
    }

    public void add(SocketClient client) {
        if (client == null) {
            return;
        }

        if (this.activeClient == null) {
            this.activeClient = client;
        }
        this.queues.add(client);
    }

    public boolean tryConnect() {
        if (this.activeClient == null) {
            return false;
        }

        SocketClient current = this.activeClient;
        do {
            if (this.activeClient.tryConnect()) {
                logger.info(this.activeClient.getName() + "> active");
                return true;
            }
        }
        while (nextClient(current));

        return false;
    }

    public void disconnect() {
        if (this.activeClient != null) {
            this.activeClient.disconnect();
        }
    }

    public void switchClient() {
        SocketClient current = this.activeClient;
        if (current != null) {
            nextClient(current);
        }
    }

    public boolean send(byte[] data) {
        return send(data, 3);
    }

    public boolean send(byte[] data, int times) {
        SocketClient current = this.activeClient;
        do {
            try {
                if (this.activeClient.tryConnect() && this.activeClient.send(data, times)) {
                    return true;
                }
                else {
                    this.activeClient.disconnect();
                }
            }
            catch (Exception ex) {
                this.activeClient.disconnect();
            }
        }
        while (nextClient(current));

        return false;
    }

    public byte[] send(byte[] data, String txId, long timeout) {
        SocketClient current = this.activeClient;
        do {
            try {
                if (this.activeClient.tryConnect()) {
                    byte[] reply = this.activeClient.send(data, txId, timeout);
                    if (reply != null) {
                        return reply;
                    }
                    else {
                        logger.error(this.activeClient.getName() + "> sned failure. tx:" + txId + ", data:" + ByteUtils.toHexString(data));
                        this.activeClient.disconnect();
                    }
                }
            }
            catch (Exception ex) {
                this.activeClient.disconnect();
            }
        }
        while (nextClient(current));

        return null;
    }

    public boolean send(byte[] data, MessageCallOut callOut, long timeout) {
        SocketClient current = this.activeClient;
        do {
            try {
                if (this.activeClient.tryConnect() && this.activeClient.send(data, callOut, timeout)) {
                    return true;
                }
                else {
                    this.activeClient.disconnect();
                }
            }
            catch (Exception ex) {
                this.activeClient.disconnect();
            }
        }
        while (nextClient(current));

        return false;
    }

    private boolean nextClient(SocketClient checkOne) {
        if (this.queues.size() < 2) {
            return false;
        }

        SocketClient client = this.queues.poll();
        this.activeClient = this.queues.peek();
        this.queues.add(client);
        return this.activeClient != checkOne;
    }
}
