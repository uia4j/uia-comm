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

/**
 * 
 * @author Kyle
 * 
 */
public class MultiProtocolMonitor<T> implements ProtocolMonitor<T> {

    private final String name;

    private final ArrayList<ProtocolMonitor<MultiProtocolMonitor<T>>> monitors;

    private final MultiProtocol<T> protocol;

    private T controller;

    /**
     * Constructor.
     * 
     * @param name Name.
     * @param protocol Protocol.
     */
    public MultiProtocolMonitor(String name, MultiProtocol<T> protocol) {
        this.protocol = protocol;
        this.name = name;
        this.monitors = new ArrayList<ProtocolMonitor<MultiProtocolMonitor<T>>>();
        for (Protocol<MultiProtocolMonitor<T>> item : protocol.protocols)
        {
            ProtocolMonitor<MultiProtocolMonitor<T>> monitor = item.createMonitor(name);
            monitor.setController(this);
            this.monitors.add(monitor);
        }
    }

    @Override
    public Protocol<T> getProtocol() {
        return this.protocol;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void read(byte data) {
        for (ProtocolMonitor<MultiProtocolMonitor<T>> monitor : this.monitors) {
            monitor.read(data);
        }
    }

    @Override
    public void readEnd() {
        for (ProtocolMonitor<MultiProtocolMonitor<T>> monitor : this.monitors) {
            monitor.readEnd();
        }
    }

    @Override
    public void reset() {
        for (ProtocolMonitor<MultiProtocolMonitor<T>> monitor : this.monitors) {
            monitor.reset();
        }
    }

    @Override
    public T getController() {
        return this.controller;
    }

    @Override
    public void setController(T controller) {
        this.controller = controller;
    }

    /**
     * Reset protocol monitor.
     * @param monitor Protocol monitor.
     */
    void reset(ProtocolMonitor<MultiProtocolMonitor<T>> monitor) {
        int idx = this.monitors.indexOf(monitor);
        if (idx >= 0) {
            for (int i = idx; i < this.monitors.size(); i++) {
                this.monitors.get(i).reset();
            }
        }
    }

}
