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
 * @param <T>
 */
public class MultiProtocolMonitor<T> implements ProtocolMonitor<T> {

    private final String name;

    private final ArrayList<ProtocolMonitor<MultiProtocolMonitor<T>>> monitors;

    private final MultiProtocol<T> protocol;

    private T controller;

    /**
     * Constructor.
     *
     * @param name Name.
     * @param protocol Protocol.
     */
    public MultiProtocolMonitor(String name, MultiProtocol<T> protocol) {
        this.protocol = protocol;
        this.name = name;
        this.monitors = new ArrayList<ProtocolMonitor<MultiProtocolMonitor<T>>>();
        for (Protocol<MultiProtocolMonitor<T>> item : protocol.protocols) {
            ProtocolMonitor<MultiProtocolMonitor<T>> monitor = item.createMonitor(name);
            monitor.setController(this);
            this.monitors.add(monitor);
        }
    }

    @Override
    public Protocol<T> getProtocol() {
        return this.protocol;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public synchronized void read(byte data) {
        int size = this.monitors.size();
        for (int i = (size - 1); i >= 0; i--) {
            this.monitors.get(i).read(data);
        }
    }

    @Override
    public void readEnd() {
        for (ProtocolMonitor<MultiProtocolMonitor<T>> monitor : this.monitors) {
            monitor.readEnd();
        }
    }

    @Override
    public void reset() {
        for (ProtocolMonitor<MultiProtocolMonitor<T>> monitor : this.monitors) {
            monitor.reset();
        }
    }

    @Override
    public T getController() {
        return this.controller;
    }

    @Override
    public void setController(T controller) {
        this.controller = controller;
    }

    @Override
    public String getStateInfo() {
        return isRunning() ? "RunningState" : "IdleState";
    }

    @Override
    public boolean isRunning() {
        for (ProtocolMonitor<MultiProtocolMonitor<T>> m : this.monitors) {
            if (m.isRunning()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Reset protocol monitor.
     * @param monitor Protocol monitor.
     */
    void reset(ProtocolMonitor<MultiProtocolMonitor<T>> monitor) {
        int idx = this.monitors.indexOf(monitor);
        if (idx >= 0) {
            for (int i = idx; i < this.monitors.size(); i++) {
                this.monitors.get(i).reset();
            }
        }
    }
}
