/*******************************************************************************
 * * Copyright (c) 2015, UIA
 * * All rights reserved.
 * * Redistribution and use in source and binary forms, with or without
 * * modification, are permitted provided that the following conditions are met:
 * *
 * *     * Redistributions of source code must retain the above copyright
 * *       notice, this list of conditions and the following disclaimer.
 * *     * Redistributions in binary form must reproduce the above copyright
 * *       notice, this list of conditions and the following disclaimer in the
 * *       documentation and/or other materials provided with the distribution.
 * *     * Neither the name of the {company name} nor the
 * *       names of its contributors may be used to endorse or promote products
 * *       derived from this software without specific prior written permission.
 * *
 * * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS "AS IS" AND ANY
 * * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package uia.comm;

import java.util.concurrent.Callable;

class MessageCallOutConcurrent implements MessageCallOut, Callable<byte[]> {

    private final String txId;

    private final long timeout;

    private byte[] result;

    private int state;  // 0: execute, 1: handled, -1: timeout.

    MessageCallOutConcurrent(String txId, long timeout) {
        this.txId = txId;
        this.timeout = timeout;
        this.state = 0;
    }

    @Override
    public byte[] call() throws Exception {
        synchronized (this.txId) {
            if (this.state != 0) {
                return this.result;
            }

            this.txId.wait(this.timeout + 20);
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
        synchronized (this.txId) {
            if (this.state == 0) {
                this.state = 1;
                this.result = reply;
            }
            this.txId.notifyAll();
        }
    }

    @Override
    public void timeout() {
        synchronized (this.txId) {
            if (this.state == 0) {
                this.state = -1;
            }
            this.txId.notifyAll();
        }
    }
}
