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
package uia.comm.protocol.htx;

import uia.comm.protocol.ProtocolEventArgs;

public class BodyState<C> implements HTxState<C> {

    private int headIdx;

    public BodyState() {
        this.headIdx = 0;
    }

    @Override
    public String toString() {
        return "BodyState";
    }

    @Override
    public void accept(HTxProtocolMonitor<C> monitor, byte one) {
        if (one == monitor.protocol.head)
        {
            this.headIdx++;
        }
        else
        {
            this.headIdx = 0;
        }

        if (this.headIdx > 0 && this.headIdx == monitor.protocol.hc)
        {
            this.headIdx = 0;
            monitor.addOne(one);
            monitor.cancelPacking(ProtocolEventArgs.ErrorCode.ERR_HEAD_REPEAT);
            for (int i = 0; i < monitor.protocol.hc; i++) {
                monitor.addOne(one);
            }
        }
        else {
            if (one == monitor.protocol.tail) {
                monitor.setState(new TailState<C>());
                monitor.read(one);
            }
            else {
                monitor.addOne(one);
            }
        }
    }
}
