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
package uia.comm.protocol;

import java.util.ArrayList;

public abstract class AbstractProtocol<C> implements Protocol<C> {

    private final ArrayList<ProtocolEventHandler<C>> handlers;

    private String aliasName;

    public AbstractProtocol() {
        this.handlers = new ArrayList<ProtocolEventHandler<C>>();
        this.aliasName = getClass().getSimpleName();
    }

    @Override
    public String getAliasName() {
        return this.aliasName;
    }

    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

    @Override
    public synchronized void addMessageHandler(ProtocolEventHandler<C> handler) {
        this.handlers.add(handler);
    }

    @Override
    public synchronized void remmoveMessageHandler(ProtocolEventHandler<C> handler) {
        this.handlers.remove(handler);
    }

    public synchronized void raiseMessageReceived(ProtocolMonitor<C> monitor, ProtocolEventArgs args) {
        for (ProtocolEventHandler<C> h : this.handlers) {
            try {
                h.messageReceived(monitor, args);
            }
            catch (Exception ex) {

            }
        }
    }

    public synchronized void raiseBorken(ProtocolMonitor<C> monitor, ProtocolEventArgs args) {
        for (ProtocolEventHandler<C> h : this.handlers) {
            try {
                h.messageError(monitor, args);
            }
            catch (Exception ex) {

            }
        }
    }
}
