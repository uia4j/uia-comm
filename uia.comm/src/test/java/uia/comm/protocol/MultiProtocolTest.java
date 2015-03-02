/*******************************************************************************
 * * Copyright (c) 2014, UIA
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

import org.junit.Test;

import uia.comm.protocol.hl.HLProtocol;
import uia.comm.protocol.ho.HOProtocol;
import uia.comm.protocol.ht.HTProtocol;
import uia.utils.ByteUtils;

public class MultiProtocolTest implements ProtocolEventHandler<Object> {

    private final MultiProtocol<Object> protocol;

    public MultiProtocolTest() {
        HOProtocol<MultiProtocolMonitor<Object>> p1 = new HOProtocol<MultiProtocolMonitor<Object>>(
                new byte[] { (byte) 0x41, (byte) 0x42, (byte) 0x43 },
                5);

        HTProtocol<MultiProtocolMonitor<Object>> p2 = new HTProtocol<MultiProtocolMonitor<Object>>(
                new byte[] { (byte) 0x8a, (byte) 0x8a },
                new byte[] { (byte) 0xa8, (byte) 0xa8 });

        ArrayList<Protocol<MultiProtocolMonitor<Object>>> ps = new ArrayList<Protocol<MultiProtocolMonitor<Object>>>();
        ps.add(p1);
        ps.add(p2);

        this.protocol = new MultiProtocol<Object>(ps);
        this.protocol.addMessageHandler(this);
    }

    @Test
    public void testNormal() {
        ProtocolMonitor<Object> monitor = this.protocol.createMonitor("abc");
        monitor.read((byte) 0x41);
        monitor.read((byte) 0x42);
        monitor.read((byte) 0x43);
        monitor.read((byte) 0x44);
        monitor.read((byte) 0x45);

        monitor.read((byte) 0x8a);
        monitor.read((byte) 0x8a);
        monitor.read((byte) 0x44);
        monitor.read((byte) 0x45);
        monitor.read((byte) 0x46);
        monitor.read((byte) 0xa8);
        monitor.read((byte) 0xa8);
    }

    @Test
    public void testEx1() {
        ProtocolMonitor<Object> monitor = this.protocol.createMonitor("abc");
        monitor.read((byte) 0x8a);
        monitor.read((byte) 0x8a);

        monitor.read((byte) 0x41);
        monitor.read((byte) 0x42);
        monitor.read((byte) 0x43);
        monitor.read((byte) 0x44);
        monitor.read((byte) 0x45);

        monitor.read((byte) 0x46);
        monitor.read((byte) 0x8a);
        monitor.read((byte) 0x8a);
        monitor.read((byte) 0x47);
        monitor.read((byte) 0x48);
        monitor.read((byte) 0xa8);
        monitor.read((byte) 0xa8);
    }

    @Test
    public void testCase1() {
        // SOH
        HLProtocol<MultiProtocolMonitor<Object>> soh =
                new HLProtocol<MultiProtocolMonitor<Object>>(
                        5,
                        1,
                        3,
                        2,
                        new LenReader() {

                            @Override
                            public int read(byte[] data) {
                                return ByteUtils.shortValue(data);
                            }

                        },
                        new byte[] { 0x10, 0x01 });
        soh.setAliasName("SOH");
        // ACK
        HOProtocol<MultiProtocolMonitor<Object>> ack =
                new HOProtocol<MultiProtocolMonitor<Object>>(
                        new byte[] { 0x10, 0x06 },
                        4);
        ack.setAliasName("ACK");
        // NAK
        HOProtocol<MultiProtocolMonitor<Object>> nak =
                new HOProtocol<MultiProtocolMonitor<Object>>(
                        new byte[] { 0x10, 0x15 },
                        5);
        nak.setAliasName("NAK");

        ArrayList<Protocol<MultiProtocolMonitor<Object>>> sub =
                new ArrayList<Protocol<MultiProtocolMonitor<Object>>>();
        sub.add(soh);
        sub.add(ack);
        sub.add(nak);

        MultiProtocol<Object> protocol = new MultiProtocol<Object>(sub);
        protocol.addMessageHandler(this);

        ProtocolMonitor<Object> monitor = protocol.createMonitor("THSRC");
        monitor.read((byte) 0x10);
        monitor.read((byte) 0x06);
        monitor.read((byte) 0x0f);
        monitor.read((byte) 0x19);
        monitor.read((byte) 0x10);
        monitor.read((byte) 0x01);
        monitor.read((byte) 0x10);
        monitor.read((byte) 0x45);
    }

    @Override
    public void messageReceived(ProtocolMonitor<Object> monitor, ProtocolEventArgs args) {
        System.out.println("r:len=" + args.getData().length + ", " + ByteUtils.toHexString(args.getData()));
    }

    @Override
    public void messageError(ProtocolMonitor<Object> monitor, ProtocolEventArgs args) {
        System.out.println("e:" + args.getErrorCode() + ",len=" + args.getData().length + ", " + ByteUtils.toHexString(args.getData()));
    }
}
