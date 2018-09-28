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
package uia.comm.protocol.hl;

import uia.comm.protocol.AbstractProtocolMonitor;
import uia.comm.protocol.ProtocolEventArgs;

/**
 *
 * @author Kyle K. Lin
 *
 * @param <T> Reference.
 */
public class HLProtocolMonitor<T> extends AbstractProtocolMonitor<T> {

    int headIdx;

    final HLProtocol<T> protocol;

    private HLState<T> state;

    public HLProtocolMonitor(String name, HLProtocol<T> protocol) {
        super(name);

        this.protocol = protocol;
        this.state = new IdleState<T>();
    }

    @Override
    public String getStateInfo() {
        return getState().toString();
    }

    public HLState<T> getState()
    {
        return this.state;
    }

    public void setState(HLState<T> state) {
        this.state = state;
    }

    @Override
    public void read(byte one) {
        this.state.accept(this, one);
    }

    @Override
    public void reset() {
        this.headIdx = 0;
        this.state = new IdleState<T>();
        this.data.clear();
    }

    @Override
    public boolean isRunning() {
        return !(this.state instanceof IdleState);
    }

    int readLenFromLeField() {
        byte[] data = new byte[this.protocol.lenFieldByteCount];
        for (int i = 0; i < data.length; i++)
        {
            data[i] = this.data.get(this.protocol.lenFieldStartIdx + i);
        }

        return this.protocol.reader.read(data);
    }

    void addOne(byte one) {
        this.data.add(one);
    }

    void cancelPacking(ProtocolEventArgs.ErrorCode errorCode)
    {
        ProtocolEventArgs args = new ProtocolEventArgs(packing(), errorCode);
        this.data.clear();
        this.protocol.raiseMessageError(this, args);
    }

    void finishPacking()
    {
        ProtocolEventArgs args = new ProtocolEventArgs(packing());
        this.data.clear();
        this.protocol.raiseMessageReceived(this, args);
    }
}
