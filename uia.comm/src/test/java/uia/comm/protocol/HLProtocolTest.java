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
package uia.comm.protocol;

import org.junit.Assert;
import org.junit.Test;

import uia.comm.protocol.hl.HLProtocol;

/**
 *
 * @author Kyle K. Lin
 *
 */
public class HLProtocolTest extends AbstractProtocolTest {

    public HLProtocolTest() {
    }

    @Test
    public void testNormal() {
        HLProtocol<Object> protocol = new HLProtocol<Object>(
                5,  // lenStartOffset
                1,  // lenEndOffset
                3,  // lenFieldIndex
                2,  // lenFiedlCount
                new LenReader() {

                    @Override
                    public int read(byte[] data) {
                        int len = data[0] << 8;
                        len += data[1];
                        return len;
                    }
                },
                new byte[] { 0x10, 0x01 });
        protocol.addMessageHandler(this);

        ProtocolMonitor<Object> monitor = protocol.createMonitor("abc");

        Assert.assertNull(this.recvArgs);
        Assert.assertNull(this.errArgs);
        Assert.assertEquals("IdleState", monitor.getStateInfo());
        monitor.read((byte) 0x00);
        Assert.assertEquals("IdleState", monitor.getStateInfo());
        monitor.read((byte) 0x00);
        Assert.assertEquals("IdleState", monitor.getStateInfo());
        monitor.read((byte) 0x00);
        Assert.assertEquals("IdleState", monitor.getStateInfo());
        monitor.read((byte) 0x10);	// head
        Assert.assertEquals("HeadState", monitor.getStateInfo());
        monitor.read((byte) 0x00);
        Assert.assertEquals("IdleState", monitor.getStateInfo());
        Assert.assertNotNull(this.errArgs);

        this.errArgs = null;
        monitor.read((byte) 0x10);	// head
        Assert.assertEquals("HeadState", monitor.getStateInfo());
        monitor.read((byte) 0x01);	// head
        Assert.assertEquals("BodyState", monitor.getStateInfo());
        monitor.read((byte) 0x01);
        Assert.assertEquals("BodyState", monitor.getStateInfo());
        monitor.read((byte) 0x00);	// length
        Assert.assertEquals("BodyState", monitor.getStateInfo());
        monitor.read((byte) 0x08);	// length
        Assert.assertEquals("BodyState", monitor.getStateInfo());
        monitor.read((byte) 0x22);
        monitor.read((byte) 0x07);
        monitor.read((byte) 0x10);
        Assert.assertEquals("BodyState", monitor.getStateInfo());
        monitor.read((byte) 0x01);
        Assert.assertEquals("BodyState", monitor.getStateInfo());
        Assert.assertNull(this.errArgs);
        monitor.read((byte) 0x0a);
        monitor.read((byte) 0x0b);
        monitor.read((byte) 0x0b);
        monitor.read((byte) 0x34);
        Assert.assertEquals("BodyState", monitor.getStateInfo());
        monitor.read((byte) 0xc7);
        Assert.assertEquals("IdleState", monitor.getStateInfo());

        Assert.assertEquals(14, this.recvArgs.getData().length, 0);

        protocol.remmoveMessageHandler(this);
    }

    @Test
    public void testEx1() {
        HLProtocol<Object> protocol = new HLProtocol<Object>(
                5,  // lenStartOffset
                1,  // lenEndOffset
                3,  // lenFieldIndex
                2,  // lenFiedlCount
                new LenReader() {

                    @Override
                    public int read(byte[] data) {
                        int len = data[0] << 8;
                        len += data[1];
                        return len;
                    }
                },
                new byte[] { 0x10, 0x01 },
                true);
        protocol.addMessageHandler(this);

        ProtocolMonitor<Object> monitor = protocol.createMonitor("abc");

        monitor.read((byte) 0x00);
        Assert.assertEquals("IdleState", monitor.getStateInfo());
        monitor.read((byte) 0x10);  // head
        Assert.assertEquals("HeadState", monitor.getStateInfo());
        monitor.read((byte) 0x01);  // head
        Assert.assertEquals("BodyState", monitor.getStateInfo());
        monitor.read((byte) 0x01);
        Assert.assertEquals("BodyState", monitor.getStateInfo());
        monitor.read((byte) 0x00);  // length
        Assert.assertEquals("BodyState", monitor.getStateInfo());
        monitor.read((byte) 0x08);  // length
        Assert.assertEquals("BodyState", monitor.getStateInfo());
        monitor.read((byte) 0x22);
        monitor.read((byte) 0x07);
        monitor.read((byte) 0xde);
        monitor.read((byte) 0x06);
        monitor.read((byte) 0x0a);

        Assert.assertNull(this.errArgs);
        monitor.read((byte) 0x10);  // head
        Assert.assertEquals("BodyState", monitor.getStateInfo());
        monitor.read((byte) 0x01);  // head
        Assert.assertEquals("BodyState", monitor.getStateInfo());
        Assert.assertNotNull(this.errArgs);
        Assert.assertNull(this.recvArgs);

        monitor.read((byte) 0x01);
        Assert.assertEquals("BodyState", monitor.getStateInfo());
        monitor.read((byte) 0x00);  // length
        Assert.assertEquals("BodyState", monitor.getStateInfo());
        monitor.read((byte) 0x08);  // length
        Assert.assertEquals("BodyState", monitor.getStateInfo());
        monitor.read((byte) 0x22);
        monitor.read((byte) 0x07);
        monitor.read((byte) 0xde);
        monitor.read((byte) 0x06);
        monitor.read((byte) 0x0a);
        monitor.read((byte) 0x00);
        monitor.read((byte) 0x1c);
        monitor.read((byte) 0x34);
        Assert.assertEquals("BodyState", monitor.getStateInfo());
        monitor.read((byte) 0xc7);
        Assert.assertEquals("IdleState", monitor.getStateInfo());

        Assert.assertEquals(14, this.recvArgs.getData().length, 0);

        protocol.remmoveMessageHandler(this);
    }
}
