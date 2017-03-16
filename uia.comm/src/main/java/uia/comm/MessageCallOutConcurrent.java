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
package uia.comm;

import java.util.concurrent.Callable;

/**
 *
 * @author Kyle K. Lin
 *
 */
class MessageCallOutConcurrent implements MessageCallOut, Callable<byte[]> {

    private final String txId;

    private final long timeout;

    private byte[] result;

    private int state;  // 0: execute, 1: handled, -1: timeout.

    private final Object key = new Object();

    MessageCallOutConcurrent(String txId, long timeout) {
        this.txId = txId;
        this.timeout = timeout;
        this.state = 0;
    }

    @Override
    public byte[] call() throws Exception {
        synchronized (this.key) {
            if (this.state != 0) {
                return this.result;
            }

            this.key.wait(this.timeout + 100);
            if (this.state == 0) {
                this.state = -1;
            }
        }
        return this.result;
    }

    @Override
    public String getTxId() {
        return this.txId;
    }

    @Override
    public void execute(byte[] reply) {
        synchronized (this.key) {
            if (this.state == 0) {
                this.state = 1;
                this.result = reply;
            }
            this.key.notifyAll();
        }
    }

    @Override
    public void timeout() {
        synchronized (this.key) {
            if (this.state == 0) {
                this.state = -1;
            }
            this.key.notifyAll();
        }
    }
}
