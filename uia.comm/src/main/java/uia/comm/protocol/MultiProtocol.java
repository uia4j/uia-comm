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

import java.util.List;

/**
 *
 * @author Kyle K. Lin
 *
 * @param <T> The reference data type.
 */
public class MultiProtocol<T> extends AbstractProtocol<T> {

    // private final static Logger logger = Logger.getLogger(MultiProtocol.class);

    final List<Protocol<MultiProtocolMonitor<T>>> protocols;

    /**
     * Constructor.
     *
     * @param protocols Protocols combined together.
     */
    public MultiProtocol(final List<Protocol<MultiProtocolMonitor<T>>> protocols) {
        this.protocols = protocols;
        for (Protocol<MultiProtocolMonitor<T>> protocol : this.protocols) {
            protocol.addMessageHandler(new ProtocolEventHandler<MultiProtocolMonitor<T>>() {

                @Override
                public void messageReceived(ProtocolMonitor<MultiProtocolMonitor<T>> monitor, ProtocolEventArgs args) {
                    raiseMessageReceived(monitor.getController(), args);
                    monitor.getController().reset(monitor);
                }

                @Override
                public void messageError(ProtocolMonitor<MultiProtocolMonitor<T>> monitor, ProtocolEventArgs args) {
                    if (MultiProtocol.this.protocols.indexOf(monitor.getProtocol()) == 0) {
                        // raiseBorken(monitor.getController(), args);
                    }
                    else {
                        /**
                         * logger.debug(String.format("multiProtocol:%s pack message error", monitor.getProtocol().getAliasName()));
                         */
                    }
                }

            });
        }
    }

    @Override
    public ProtocolMonitor<T> createMonitor(String name) {
        return new MultiProtocolMonitor<T>(name, this);
    }

}
