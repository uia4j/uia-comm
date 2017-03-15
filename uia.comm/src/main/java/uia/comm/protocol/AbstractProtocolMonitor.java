/*******************************************************************************
 * * Copyright (c) 2015, UIA * All rights reserved. * Redistribution and use in source and binary forms, with or without * modification, are permitted provided that the following conditions are met: * * * Redistributions of source code must retain
 * the above copyright * notice, this list of conditions and the following disclaimer. * * Redistributions in binary form must reproduce the above copyright * notice, this list of conditions and the following disclaimer in the * documentation and/or
 * other materials provided with the distribution. * * Neither the name of the {company name} nor the * names of its contributors may be used to endorse or promote products * derived from this software without specific prior written permission. * *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS "AS IS" AND ANY * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE * DISCLAIMED. IN NO
 * EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; * LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS * SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package uia.comm.protocol;

import java.util.ArrayList;

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
        System.gc();
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
        System.gc();

        return result;
    }
}
