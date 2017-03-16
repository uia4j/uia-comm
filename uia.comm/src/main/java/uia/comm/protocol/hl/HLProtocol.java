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

import uia.comm.protocol.AbstractProtocol;
import uia.comm.protocol.LenReader;
import uia.comm.protocol.ProtocolMonitor;

/**
 *
 * @author Kyle K. Lin
 *
 * @param <C>
 */
public class HLProtocol<T> extends AbstractProtocol<T> {

    final int lenStartOffset;

    final int lenEndOffset;

    final int lenFieldStartIdx;

    final int lenFieldByteCount;

    final LenReader reader;

    final byte[] head;

    final boolean strict;

    public HLProtocol(
            int lenStartOffset,
            int lenEndOffset,
            int lenFieldIdx,
            int lenFieldCount,
            LenReader reader,
            byte[] head) {
        this(lenStartOffset, lenEndOffset, lenFieldIdx, lenFieldCount, reader, head, false);
    }

    public HLProtocol(
            int lenStartOffset,
            int lenEndOffset,
            int lenFieldIdx,
            int lenFieldCount,
            LenReader reader,
            byte[] head,
            boolean strict) {
        this.lenStartOffset = lenStartOffset;
        this.lenEndOffset = lenEndOffset;
        this.lenFieldStartIdx = lenFieldIdx;
        this.lenFieldByteCount = lenFieldCount;
        this.reader = reader;
        this.head = head;
        this.strict = strict;
    }

    public int getLenFieldStartIdx() {
        return this.lenFieldStartIdx;
    }

    public int getLenFieldEndIdx() {
        return this.lenFieldStartIdx + this.lenFieldByteCount;
    }

    @Override
    public ProtocolMonitor<T> createMonitor(String name) {
        HLProtocolMonitor<T> monitor = new HLProtocolMonitor<T>(name, this);
        monitor.setProtocol(this);
        return monitor;
    }

}
