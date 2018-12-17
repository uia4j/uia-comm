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
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 *
 * @author Kyle K. Lin
 *
 */
public class SocketDataSelector {

    private boolean started;

    private final Selector selector;

    /**
     *
     * @throws IOException Raise when open failed.
     */
    public SocketDataSelector() throws IOException {
        this.selector = Selector.open();
    }

    /**
     * Register controller to specific channel.
     * @param ch Socket channel.
     * @param controller Socket data controller.
     * @throws ClosedChannelException Raise when register channel failed.
     */
    public void register(SocketChannel ch, SocketDataController controller) throws ClosedChannelException {
        ch.register(this.selector, SelectionKey.OP_READ, controller);
    }

    /**
     * Start selector.
     */
    public void start() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                running();
            }

        }).start();
    }

    /**
     * Stop selector.
     */
    public void stop() {
        this.started = false;
        this.selector.wakeup();
    }

    private void running() {
        while (this.started) {
            try {
                this.selector.select();
            }
            catch (Exception ex) {
            }

            Iterator<SelectionKey> iter = this.selector.selectedKeys().iterator();
            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                iter.remove();

                if (key.isReadable()) {
                    SocketDataController controller = (SocketDataController) key.attachment();
                    try {
                        controller.receive();
                    }
                    catch (IOException e) {
                        // TODO: raise event to disconnect
                    }
                }
            }
        }
    }
}
