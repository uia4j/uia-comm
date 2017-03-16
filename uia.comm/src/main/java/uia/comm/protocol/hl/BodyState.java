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

import uia.comm.protocol.ProtocolEventArgs;

public class BodyState<T> implements HLState<T> {

    private int len;

    private int headIdx;

    public BodyState() {
        this.len = -1;
        this.headIdx = 0;
    }

    @Override
    public String toString() {
        return "BodyState";
    }

    @Override
    public void accept(HLProtocolMonitor<T> monitor, byte one) {
        if (monitor.protocol.strict && one == monitor.protocol.head[this.headIdx]) {
            this.headIdx++;
        }
        else {
            this.headIdx = 0;
        }

        if (this.headIdx > 0 && this.headIdx == monitor.protocol.head.length) {
            this.headIdx = 0;
            monitor.addOne(one);
            monitor.cancelPacking(ProtocolEventArgs.ErrorCode.ERR_HEAD_REPEAT);
            for (byte b : monitor.protocol.head) {
                monitor.addOne(b);
            }
        }
        else {
            monitor.addOne(one);
            if (monitor.getDataLength() == monitor.protocol.getLenFieldEndIdx()) {
                this.len = monitor.readLenFromLeField();
            }

            if (monitor.getDataLength() > monitor.protocol.getLenFieldEndIdx() && this.len < 0) {
                monitor.cancelPacking(ProtocolEventArgs.ErrorCode.ERR_BODY_LENGTH);
                monitor.setState(new IdleState<T>());
                return;
            }

            if (this.len >= 0 && (monitor.protocol.lenStartOffset + this.len + monitor.protocol.lenEndOffset) == monitor.getDataLength()) {
                monitor.finishPacking();
                monitor.setState(new IdleState<T>());
            }
        }
    }
}
