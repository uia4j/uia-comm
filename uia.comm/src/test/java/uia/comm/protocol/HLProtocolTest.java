/*******************************************************************************
 * * Copyright (c) 2014, UIA * All rights reserved. * Redistribution and use in source and binary forms, with or without * modification, are permitted provided that the following conditions are met: * * * Redistributions of source code must retain
 * the above copyright * notice, this list of conditions and the following disclaimer. * * Redistributions in binary form must reproduce the above copyright * notice, this list of conditions and the following disclaimer in the * documentation and/or
 * other materials provided with the distribution. * * Neither the name of the {company name} nor the * names of its contributors may be used to endorse or promote products * derived from this software without specific prior written permission. * *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS "AS IS" AND ANY * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE * DISCLAIMED. IN NO
 * EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; * LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS * SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package uia.comm.protocol;

import java.util.ArrayList;

import org.junit.Test;

import uia.comm.protocol.hl.HLProtocol;
import uia.comm.protocol.ho.HOProtocol;
import uia.utils.ByteUtils;
import uia.utils.HexStringUtils;

public class HLProtocolTest implements ProtocolEventHandler<Object> {

    private final HLProtocol<Object> protocol;

    public HLProtocolTest() {
        this.protocol = new HLProtocol<Object>(
                5,
                1,
                3,
                2,
                new LenReader() {

                    @Override
                    public int read(byte[] data) {
                        int len = data[0] << 8;
                        len += data[1];
                        return len;
                    }
                },
                new byte[] { 0x10, 0x01 });
        this.protocol.addMessageHandler(this);
    }

    @Test
    public void testNormal1() {
        ProtocolMonitor<Object> monitor = this.protocol.createMonitor("abc");
        monitor.read((byte) 0x10);
        monitor.read((byte) 0x01);
        monitor.read((byte) 0x01);
        monitor.read((byte) 0x00);
        monitor.read((byte) 0x08);
        monitor.read((byte) 0x22);
        monitor.read((byte) 0x07);
        monitor.read((byte) 0xde);
        monitor.read((byte) 0x06);
        monitor.read((byte) 0x0a);
        monitor.read((byte) 0x00);
        monitor.read((byte) 0x1c);
        monitor.read((byte) 0x34);
        monitor.read((byte) 0xc7);
    }

    @Test
    public void testPIS() {
        String data = "10-01-01-12-1a-82-01-1e-0f-01-ff-00-00-12-10-01-08-03-01-00-00-00-f6-00-00-01-1e-01-17-d4-d4-ff-00-00-00-03-30-17-ff-d4-d4-00-00-00-00-02-17-ff-00-00-00-00-00-08-2b-00-00-17-a8-a8-a8-00-00-00-01-01-80-80-80-80-80-80-d4-d4-ff-a8-a8-ff-80-80-80-80-80-80-ff-ff-ff-80-80-80-02-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-d4-d4-ff-a8-a8-ff-ff-ff-ff-80-80-80-03-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-00-25-17-d4-ff-d4-00-00-00-00-0a-a5-fe-a8-ae-a6-db-a5-d1-ae-79-14-17-d4-ff-d4-00-00-00-00-08-a7-59-b1-4e-c2-f7-af-b8-02-17-d4-d4-ff-00-00-00-03-30-17-ff-d4-d4-00-00-00-00-02-17-ff-00-00-00-00-00-08-2b-00-00-17-a8-a8-a8-00-00-00-01-01-80-80-80-80-80-80-d4-d4-ff-a8-a8-ff-80-80-80-80-80-80-ff-ff-ff-80-80-80-02-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-d4-d4-ff-a8-a8-ff-ff-ff-ff-80-80-80-03-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-00-4d-17-d4-ff-d4-00-00-00-00-1e-00-41-00-6c-00-6c-00-20-00-4e-00-6f-00-6e-00-2d-00-52-00-73-00-65-00-72-00-76-00-65-00-64-14-17-d4-ff-d4-00-00-00-00-1c-00-4e-00-65-00-61-00-72-00-20-00-44-00-65-00-70-00-61-00-72-00-74-00-75-00-72-00-65-02-00-00-00-f6-00-00-01-20-01-17-d4-d4-ff-00-00-00-03-34-17-ff-d4-d4-00-00-00-00-02-17-ff-00-00-00-00-00-09-0d-03-e7-17-a8-a8-a8-00-00-00-01-01-80-80-80-80-80-80-d4-d4-ff-a8-a8-ff-80-80-80-80-80-80-ff-ff-ff-80-80-80-02-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-d4-d4-ff-a8-a8-ff-ff-ff-ff-80-80-80-03-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-00-25-17-d4-ff-d4-00-00-00-00-0a-a5-fe-a8-ae-a6-db-a5-d1-ae-79-14-17-d4-ff-d4-00-00-00-00-08-a9-b5-bf-f0-b5-6f-a8-ae-02-17-d4-d4-ff-00-00-00-03-34-17-ff-d4-d4-00-00-00-00-02-17-ff-00-00-00-00-00-09-0d-03-e7-17-a8-a8-a8-00-00-00-01-01-80-80-80-80-80-80-d4-d4-ff-a8-a8-ff-80-80-80-80-80-80-ff-ff-ff-80-80-80-02-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-d4-d4-ff-a8-a8-ff-ff-ff-ff-80-80-80-03-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-00-4f-17-d4-ff-d4-00-00-00-00-1e-00-41-00-6c-00-6c-00-20-00-4e-00-6f-00-6e-00-2d-00-52-00-73-00-65-00-72-00-76-00-65-00-64-14-17-d4-ff-d4-00-00-00-00-1e-00-44-00-65-00-70-00-61-00-72-00-74-00-75-00-72-00-65-00-20-00-44-00-65-00-6c-00-61-00-79-03-00-00-00-f4-00-00-01-10-01-17-d4-d4-ff-00-00-00-03-38-17-ff-d4-d4-00-00-00-00-02-17-ff-00-00-00-00-00-09-2b-00-00-17-a8-a8-a8-00-00-00-01-01-80-80-80-80-80-80-d4-d4-ff-a8-a8-ff-80-80-80-80-80-80-ff-ff-ff-80-80-80-02-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-d4-d4-ff-a8-a8-ff-ff-ff-ff-80-80-80-03-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-00-23-17-d4-ff-d4-00-00-00-00-0a-a5-fe-a8-ae-a6-db-a5-d1-ae-79-14-17-d4-ff-d4-00-00-00-00-06-a4-77-b6-69-af-b8-02-17-d4-d4-ff-00-00-00-03-38-17-ff-d4-d4-00-00-00-00-02-17-ff-00-00-00-00-00-09-2b-00-00-17-a8-a8-a8-00-00-00-01-01-80-80-80-80-80-80-d4-d4-ff-a8-a8-ff-80-80-80-80-80-80-ff-ff-ff-80-80-80-02-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-d4-d4-ff-a8-a8-ff-ff-ff-ff-80-80-80-03-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-00-3f-17-d4-ff-d4-00-00-00-00-1e-00-41-00-6c-00-6c-00-20-00-4e-00-6f-00-6e-00-2d-00-52-00-73-00-65-00-72-00-76-00-65-00-64-14-17-d4-ff-d4-00-00-00-00-0e-00-41-00-72-00-72-00-69-00-76-00-61-00-6c-04-00-00-00-f2-00-00-01-0e-01-17-d4-d4-ff-00-00-00-03-3c-17-ff-d4-d4-00-00-00-00-02-17-ff-00-00-00-00-00-0a-0d-00-00-17-a8-a8-a8-00-00-00-01-01-80-80-80-80-80-80-d4-d4-ff-a8-a8-ff-80-80-80-80-80-80-ff-ff-ff-80-80-80-02-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-d4-d4-ff-a8-a8-ff-ff-ff-ff-80-80-80-03-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-00-21-17-d4-ff-d4-00-00-00-00-0a-a5-fe-a8-ae-a6-db-a5-d1-ae-79-14-17-d4-ff-d4-00-00-00-00-04-a8-fa-ae-f8-02-17-d4-d4-ff-00-00-00-03-3c-17-ff-d4-d4-00-00-00-00-02-17-ff-00-00-00-00-00-0a-0d-00-00-17-a8-a8-a8-00-00-00-01-01-80-80-80-80-80-80-d4-d4-ff-a8-a8-ff-80-80-80-80-80-80-ff-ff-ff-80-80-80-02-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-d4-d4-ff-a8-a8-ff-ff-ff-ff-80-80-80-03-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-00-3d-17-d4-ff-d4-00-00-00-00-1e-00-41-00-6c-00-6c-00-20-00-4e-00-6f-00-6e-00-2d-00-52-00-73-00-65-00-72-00-76-00-65-00-64-14-17-d4-ff-d4-00-00-00-00-0c-00-43-00-61-00-6e-00-63-00-65-00-6c-05-00-00-00-f6-00-00-01-1e-01-17-d4-d4-ff-00-00-00-03-31-17-ff-d4-d4-00-00-00-08-00-17-ff-00-00-00-00-00-09-1f-00-00-17-a8-a8-a8-00-00-00-02-00-80-80-80-80-80-80-d4-d4-ff-a8-a8-ff-80-80-80-80-80-80-ff-ff-ff-80-80-80-02-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-d4-d4-ff-a8-a8-ff-ff-ff-ff-80-80-80-03-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-00-25-17-d4-ff-d4-00-00-00-00-0a-a5-fe-a8-ae-a6-db-a5-d1-ae-79-14-17-d4-ff-d4-00-00-00-00-08-a7-59-b1-4e-c2-f7-af-b8-02-17-d4-d4-ff-00-00-00-03-31-17-ff-d4-d4-00-00-00-08-00-17-ff-00-00-00-00-00-09-1f-00-00-17-a8-a8-a8-00-00-00-02-00-80-80-80-80-80-80-d4-d4-ff-a8-a8-ff-80-80-80-80-80-80-ff-ff-ff-80-80-80-02-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-d4-d4-ff-a8-a8-ff-ff-ff-ff-80-80-80-03-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-00-4d-17-d4-ff-d4-00-00-00-00-1e-00-41-00-6c-00-6c-00-20-00-4e-00-6f-00-6e-00-2d-00-52-00-73-00-65-00-72-00-76-00-65-00-64-14-17-d4-ff-d4-00-00-00-00-1c-00-4e-00-65-00-61-00-72-00-20-00-44-00-65-00-70-00-61-00-72-00-74-00-75-00-72-00-65-06-00-00-01-1a-00-00-01-4e-01-17-d4-d4-ff-00-00-00-03-35-17-ff-d4-d4-00-00-00-08-00-17-ff-00-00-00-00-00-0a-01-00-0c-17-a8-a8-a8-00-00-00-02-00-80-80-80-80-80-80-d4-d4-ff-a8-a8-ff-80-80-80-80-80-80-ff-ff-ff-80-80-80-02-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-d4-d4-ff-a8-a8-ff-ff-ff-ff-80-80-80-03-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-00-49-17-d4-ff-d4-00-00-00-00-0a-a5-fe-a8-ae-a6-db-a5-d1-ae-79-14-17-d4-ff-d4-00-00-00-00-2c-a9-b5-bf-f0-00-3a-00-20-00-31-00-32-00-20-a4-c0-c4-c1-a1-41-b9-77-ad-70-00-20-00-30-00-39-00-3a-00-35-00-38-00-20-a9-e8-b9-46-a1-43-02-17-d4-d4-ff-00-00-00-03-35-17-ff-d4-d4-00-00-00-08-00-17-ff-00-00-00-00-00-0a-01-00-0c-17-a8-a8-a8-00-00-00-02-00-80-80-80-80-80-80-d4-d4-ff-a8-a8-ff-80-80-80-80-80-80-ff-ff-ff-80-80-80-02-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-d4-d4-ff-a8-a8-ff-ff-ff-ff-80-80-80-03-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-00-7d-17-d4-ff-d4-00-00-00-00-1e-00-41-00-6c-00-6c-00-20-00-4e-00-6f-00-6e-00-2d-00-52-00-73-00-65-00-72-00-76-00-65-00-64-14-17-d4-ff-d4-00-00-00-00-4c-00-44-00-65-00-6c-00-61-00-79-00-3a-00-20-00-31-00-32-00-20-00-4d-00-69-00-6e-00-73-00-2c-00-20-00-57-00-69-00-6c-00-6c-00-20-00-41-00-72-00-72-00-69-00-76-00-61-00-6c-00-20-00-41-00-74-00-20-00-30-00-39-00-3a-00-35-00-38-00-2e-07-00-00-00-f2-00-00-01-10-01-17-d4-d4-ff-00-00-00-03-39-17-ff-d4-d4-00-00-00-08-00-17-ff-00-00-00-00-00-0a-1f-00-00-17-a8-a8-a8-00-00-00-02-00-80-80-80-80-80-80-d4-d4-ff-a8-a8-ff-80-80-80-80-80-80-ff-ff-ff-80-80-80-02-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-d4-d4-ff-a8-a8-ff-ff-ff-ff-80-80-80-03-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-00-21-17-d4-ff-d4-00-00-00-00-0a-a5-fe-a8-ae-a6-db-a5-d1-ae-79-14-17-d4-ff-d4-00-00-00-00-04-b7-c7-c2-49-02-17-d4-d4-ff-00-00-00-03-39-17-ff-d4-d4-00-00-00-08-00-17-ff-00-00-00-00-00-0a-1f-00-00-17-a8-a8-a8-00-00-00-02-00-80-80-80-80-80-80-d4-d4-ff-a8-a8-ff-80-80-80-80-80-80-ff-ff-ff-80-80-80-02-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-d4-d4-ff-a8-a8-ff-ff-ff-ff-80-80-80-03-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-00-3f-17-d4-ff-d4-00-00-00-00-1e-00-41-00-6c-00-6c-00-20-00-4e-00-6f-00-6e-00-2d-00-52-00-73-00-65-00-72-00-76-00-65-00-64-14-17-d4-ff-d4-00-00-00-00-0e-00-4f-00-6e-00-20-00-54-00-69-00-6d-00-65-08-00-00-00-f2-00-00-01-10-01-17-d4-d4-ff-00-00-00-03-3d-17-ff-d4-d4-00-00-00-08-00-17-ff-00-00-00-00-00-0b-01-00-00-17-a8-a8-a8-00-00-00-02-00-80-80-80-80-80-80-d4-d4-ff-a8-a8-ff-80-80-80-80-80-80-ff-ff-ff-80-80-80-02-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-d4-d4-ff-a8-a8-ff-ff-ff-ff-80-80-80-03-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-00-21-17-d4-ff-d4-00-00-00-00-0a-a5-fe-a8-ae-a6-db-a5-d1-ae-79-14-17-d4-ff-d4-00-00-00-00-04-b7-c7-c2-49-02-17-d4-d4-ff-00-00-00-03-3d-17-ff-d4-d4-00-00-00-08-00-17-ff-00-00-00-00-00-0b-01-00-00-17-a8-a8-a8-00-00-00-02-00-80-80-80-80-80-80-d4-d4-ff-a8-a8-ff-80-80-80-80-80-80-ff-ff-ff-80-80-80-02-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-80-80-80-80-80-80-ff-ff-ff-80-80-80-01-d4-d4-ff-a8-a8-ff-ff-ff-ff-80-80-80-03-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-d4-d4-ff-d4-d4-ff-ff-ff-ff-80-80-80-04-00-3f-17-d4-ff-d4-00-00-00-00-1e-00-41-00-6c-00-6c-00-20-00-4e-00-6f-00-6e-00-2d-00-52-00-73-00-65-00-72-00-76-00-65-00-64-14-17-d4-ff-d4-00-00-00-00-0e-00-4f-00-6e-00-20-00-54-00-69-00-6d-00-65-02-01-09-00-00-01-10-01-01-19-02-01-01-17-d4-ff-d4-00-00-00-00-6c-00-3c-00-63-00-6f-00-6c-00-6f-00-72-00-20-00-66-00-67-00-3d-00-22-00-32-00-35-00-35-00-3a-00-32-00-35-00-35-00-3a-00-30-00-22-00-20-00-62-00-67-00-3d-00-22-00-30-00-3a-00-30-00-3a-00-30-00-22-00-3e-a6-43-a8-ae-00-20-00-30-00-38-00-31-00-37-00-20-a7-59-b1-4e-c2-f7-af-b8-a1-43-00-3c-00-2f-00-63-00-6f-00-6c-00-6f-00-72-00-3e-02-02-17-d4-ff-d4-00-00-00-00-8a-00-3c-00-63-00-6f-00-6c-00-6f-00-72-00-20-00-66-00-67-00-3d-00-22-00-32-00-35-00-35-00-3a-00-32-00-35-00-35-00-3a-00-30-00-22-00-20-00-62-00-67-00-3d-00-22-00-30-00-3a-00-30-00-3a-00-30-00-22-00-3e-00-54-00-72-00-61-00-69-00-6e-00-20-00-30-00-38-00-31-00-37-00-20-00-77-00-69-00-6c-00-6c-00-20-00-64-00-65-00-70-00-61-00-72-00-74-00-20-00-73-00-6f-00-6f-00-6e-00-2e-00-3c-00-2f-00-63-00-6f-00-6c-00-6f-00-72-00-3e-00";
        byte[] bytes = HexStringUtils.toBytes(data, "-");

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
                        new byte[] { 0x10, 0x01 },
                        false);
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

        ProtocolMonitor<Object> monitor = protocol.createMonitor("abc");
        for (int i = 0; i < bytes.length; i++) {
            monitor.read(bytes[i]);
        }
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
