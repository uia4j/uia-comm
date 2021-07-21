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
package uia.comm.protocol;

import java.util.ArrayList;

/**
 *
 * @author Kyle K. Lin
 *
 * @param <C> The reference data type.
 */
public abstract class AbstractProtocol<C> implements Protocol<C> {

    private final ArrayList<ProtocolEventHandler<C>> handlers;

    private String aliasName;

    /**
     * Constructor.
     *
     */
    public AbstractProtocol() {
        this.handlers = new ArrayList<ProtocolEventHandler<C>>();
        this.aliasName = getClass().getSimpleName();
    }

    @Override
    public String getAliasName() {
        return this.aliasName;
    }

    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

    @Override
    public synchronized void addMessageHandler(ProtocolEventHandler<C> handler) {
        this.handlers.add(handler);
    }

    @Override
    public synchronized void remmoveMessageHandler(ProtocolEventHandler<C> handler) {
        this.handlers.remove(handler);
    }

    /**
     * Raise if structure of message is correct.
     * @param monitor Monitor.
     * @param args Event arguments.
     */
    public synchronized void raiseMessageReceived(ProtocolMonitor<C> monitor, ProtocolEventArgs args) {
        if (args.getData() == null || args.getData().length == 0) {
            return;
        }

        for (ProtocolEventHandler<C> h : this.handlers) {
            try {
                h.messageReceived(monitor, args);
            }
            catch (Exception ex) {

            }
        }
    }

    /**
     * Raise if structure of message is incorrect.
     * @param monitor Monitor.
     * @param args Event arguments.
     */
    public synchronized void raiseMessageError(ProtocolMonitor<C> monitor, ProtocolEventArgs args) {
        if (args.getData() == null || args.getData().length == 0) {
            return;
        }

        for (ProtocolEventHandler<C> h : this.handlers) {
            try {
                h.messageError(monitor, args);
            }
            catch (Exception ex) {

            }
        }
    }
}
