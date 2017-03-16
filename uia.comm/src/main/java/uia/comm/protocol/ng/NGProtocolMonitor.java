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
package uia.comm.protocol.ng;

import uia.comm.protocol.AbstractProtocolMonitor;
import uia.comm.protocol.ProtocolEventArgs;

public class NGProtocolMonitor<C> extends AbstractProtocolMonitor<C> {

    final NGProtocol<C> protocol;

    public NGProtocolMonitor(String name, NGProtocol<C> protocol) {
        super(name);
        this.protocol = protocol;
    }

    @Override
    public void read(byte one) {
        this.data.add(one);
    }

    @Override
    public void readEnd() {
        finsihPacking();
        this.data.clear();
    }

    @Override
    public void reset() {
        this.data.clear();
    }

    @Override
    public String getStateInfo() {
        return isRunning() ? "BodyState" : "IdleState";
    }

    @Override
    public boolean isRunning() {
        return this.data.size() > 0;
    }

    private void finsihPacking() {
        ProtocolEventArgs args = new ProtocolEventArgs(packing());
        this.protocol.raiseMessageReceived(this, args);
    }
}
