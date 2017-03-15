/*******************************************************************************
 * * Copyright (c) 2015, UIA * All rights reserved. * Redistribution and use in source and binary forms, with or without * modification, are permitted provided that the following conditions are met: * * * Redistributions of source code must retain
 * the above copyright * notice, this list of conditions and the following disclaimer. * * Redistributions in binary form must reproduce the above copyright * notice, this list of conditions and the following disclaimer in the * documentation and/or
 * other materials provided with the distribution. * * Neither the name of the {company name} nor the * names of its contributors may be used to endorse or promote products * derived from this software without specific prior written permission. * *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS "AS IS" AND ANY * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE * DISCLAIMED. IN NO
 * EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; * LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS * SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package uia.comm.protocol.ho;

import uia.comm.protocol.AbstractProtocolMonitor;
import uia.comm.protocol.ProtocolEventArgs;
import uia.comm.protocol.ht.HTState;

public class HOProtocolMonitor<C> extends AbstractProtocolMonitor<C> {

    final HOProtocol<C> protocol;

    int headIdx;

    private HOState<C> state;

    public HOProtocolMonitor(String name, HOProtocol<C> protocol) {
        super(name);
        this.protocol = protocol;
        this.state = new IdleState<C>();
    }

    @Override
    public void read(byte one) {
        this.state.accept(this, one);
    }

    @Override
    public void readEnd() {
        this.state.end(this);
    }

    @Override
    public void reset() {
        this.headIdx = 0;
        this.data.clear();
    }

    @Override
    public boolean isRunning() {
        return !(this.state instanceof IdleState);
    }
    
    @Override
    public String getStateInfo() {
    	return getState().toString();
    }

    public HOState<C> getState()
    {
        return this.state;
    }

    public void setState(HOState<C> state) {
        this.state = state;
    }

    void addOne(byte one) {
        this.data.add(one);
    }

    void finsihPacking() {
        ProtocolEventArgs args = new ProtocolEventArgs(packing());
        reset();
        this.protocol.raiseMessageReceived(this, args);
    }

    void cancelPacking(ProtocolEventArgs.ErrorCode errorCode) {
        ProtocolEventArgs args = new ProtocolEventArgs(packing(), errorCode);
        reset();
        this.protocol.raiseBorken(this, args);
    }
}
