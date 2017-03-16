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
package uia.comm.protocol.ht;

import uia.comm.protocol.AbstractProtocolMonitor;
import uia.comm.protocol.ProtocolEventArgs;

public class HTProtocolMonitor<C> extends AbstractProtocolMonitor<C> {

    int headIdx;

    int tailIdx;

    HTState<C> state;

    final HTProtocol<C> protocol;

    public HTProtocolMonitor(String name, HTProtocol<C> protocol) {
        super(name);
        this.state = new IdleState<C>();
        this.protocol = protocol;
    }

    @Override
    public void read(byte one) {
        this.state.accept(this, one);
    }

    @Override
    public void reset() {
        this.headIdx = 0;
        this.tailIdx = 0;
        this.data.clear();
        this.state = new IdleState<C>();
    }

    @Override
    public boolean isRunning() {
        return !(this.state instanceof IdleState);
    }

    @Override
    public String getStateInfo() {
        return getState().toString();
    }

    public HTState<C> getState()
    {
        return this.state;
    }

    public void setState(HTState<C> state) {
        this.state = state;
    }

    void addOne(byte one) {
        this.data.add(one);
    }

    void finsihPacking() {
        ProtocolEventArgs args = new ProtocolEventArgs(packing());
        this.protocol.raiseMessageReceived(this, args);
    }

    void cancelPacking(ProtocolEventArgs.ErrorCode errorCode) {
        ProtocolEventArgs args = new ProtocolEventArgs(packing(), errorCode);
        this.protocol.raiseBorken(this, args);
    }
}
