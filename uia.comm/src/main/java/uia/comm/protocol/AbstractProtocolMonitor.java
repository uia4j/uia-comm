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
public abstract class AbstractProtocolMonitor<C> implements ProtocolMonitor<C> {

    protected ArrayList<Byte> data;

    private final String name;

    private C controller;

    private Protocol<C> protocol;

    public AbstractProtocolMonitor(String name) {
        this.name = name;
        this.data = new ArrayList<Byte>();
    }

    @Override
    public Protocol<C> getProtocol() {
        return this.protocol;
    }

    public void setProtocol(Protocol<C> protocol) {
        this.protocol = protocol;
    }

    @Override
    public int getDataLength() {
        return this.data.size();
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void readEnd() {
    }

    @Override
    public void reset() {
        this.data.clear();
        this.data.trimToSize();
        // System.gc();
    }

    @Override
    public C getController() {
        return this.controller;
    }

    @Override
    public void setController(C controller) {
        this.controller = controller;
    }

    public byte[] packing() {
        byte[] result = new byte[this.data.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = this.data.get(i);
        }
        this.data.clear();
        this.data.trimToSize();
        this.data = new ArrayList<Byte>();
        // System.gc();

        return result;
    }
}
