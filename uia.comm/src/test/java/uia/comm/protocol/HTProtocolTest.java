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

import uia.comm.protocol.ht.HTProtocol;

/**
 *
 * @author Kyle K. Lin
 *
 */
public class HTProtocolTest extends AbstractProtocolTest {

    private final HTProtocol<Object> protocol;

    public HTProtocolTest() {
        this.protocol = new HTProtocol<Object>(
                new byte[] { (byte) 0x8a, (byte) 0x8a },
                new byte[] { (byte) 0xa8, (byte) 0xa8, (byte) 0xa8 });
        this.protocol.addMessageHandler(this);
    }

    @Test
    public void testNormal() {
        ProtocolMonitor<Object> monitor = this.protocol.createMonitor("abc");

        monitor.read((byte) 0x00);
        Assert.assertEquals("IdleState", monitor.getStateInfo());
        monitor.read((byte) 0x00);
        Assert.assertEquals("IdleState", monitor.getStateInfo());
        monitor.read((byte) 0x8a);	// head
        Assert.assertEquals("HeadState", monitor.getStateInfo());
        monitor.read((byte) 0x00);
        Assert.assertEquals("IdleState", monitor.getStateInfo());

        monitor.read((byte) 0x8a);	// head
        Assert.assertEquals("HeadState", monitor.getStateInfo());
        monitor.read((byte) 0x8a);	// head
        Assert.assertEquals("BodyState", monitor.getStateInfo());
        monitor.read((byte) 0x41);
        Assert.assertEquals("BodyState", monitor.getStateInfo());
        monitor.read((byte) 0x43);
        Assert.assertEquals("BodyState", monitor.getStateInfo());
        monitor.read((byte) 0x45);
        Assert.assertEquals("BodyState", monitor.getStateInfo());
        monitor.read((byte) 0xa8);	// tail
        Assert.assertEquals("TailState", monitor.getStateInfo());
        monitor.read((byte) 0xa8);  // tail
        Assert.assertEquals("TailState", monitor.getStateInfo());
        monitor.read((byte) 0xa8);	// tail
        Assert.assertEquals("IdleState", monitor.getStateInfo());

        Assert.assertArrayEquals(
                new byte[] { (byte) 0x8a, (byte) 0x8a, 0x41, 0x43, 0x45, (byte) 0xa8, (byte) 0xa8, (byte) 0xa8 },
                this.recvArgs.getData());
    }

    @Test
    public void testEx1() {
        ProtocolMonitor<Object> monitor = this.protocol.createMonitor("abc");

        Assert.assertNull(this.errArgs);

        Assert.assertEquals("IdleState", monitor.getStateInfo());
        monitor.read((byte) 0x8a);
        Assert.assertEquals("HeadState", monitor.getStateInfo());
        monitor.read((byte) 0x8a);
        Assert.assertEquals("BodyState", monitor.getStateInfo());
        monitor.read((byte) 0x41);
        Assert.assertEquals("BodyState", monitor.getStateInfo());
        monitor.read((byte) 0x43);
        Assert.assertEquals("BodyState", monitor.getStateInfo());

        monitor.read((byte) 0x8a);
        Assert.assertEquals("BodyState", monitor.getStateInfo());
        monitor.read((byte) 0x8a);
        Assert.assertEquals("BodyState", monitor.getStateInfo());
        Assert.assertNotNull(this.errArgs);

        monitor.read((byte) 0x45);
        Assert.assertEquals("BodyState", monitor.getStateInfo());
        monitor.read((byte) 0xa8);
        Assert.assertEquals("TailState", monitor.getStateInfo());
        monitor.read((byte) 0xa8);
        Assert.assertEquals("TailState", monitor.getStateInfo());
        monitor.read((byte) 0xa8);
        Assert.assertEquals("IdleState", monitor.getStateInfo());

        Assert.assertArrayEquals(
                new byte[] { (byte) 0x8a, (byte) 0x8a, 0x45, (byte) 0xa8, (byte) 0xa8, (byte) 0xa8 },
                this.recvArgs.getData());
    }

    @Test
    public void testEx2() {
        ProtocolMonitor<Object> monitor = this.protocol.createMonitor("abc");

        Assert.assertNull(this.errArgs);

        Assert.assertEquals("IdleState", monitor.getStateInfo());
        monitor.read((byte) 0x8a);
        Assert.assertEquals("HeadState", monitor.getStateInfo());
        monitor.read((byte) 0x8b);
        Assert.assertEquals("IdleState", monitor.getStateInfo());
    }

    @Test
    public void testEx3() {
        ProtocolMonitor<Object> monitor = this.protocol.createMonitor("abc");

        Assert.assertNull(this.errArgs);

        Assert.assertEquals("IdleState", monitor.getStateInfo());
        monitor.read((byte) 0x8a);
        Assert.assertEquals("HeadState", monitor.getStateInfo());
        monitor.read((byte) 0x8a);
        Assert.assertEquals("BodyState", monitor.getStateInfo());
        monitor.read((byte) 0x41);
        Assert.assertEquals("BodyState", monitor.getStateInfo());
        monitor.read((byte) 0x43);
        Assert.assertEquals("BodyState", monitor.getStateInfo());
        monitor.read((byte) 0x45);
        Assert.assertEquals("BodyState", monitor.getStateInfo());
        monitor.read((byte) 0xa8);
        Assert.assertEquals("TailState", monitor.getStateInfo());

        monitor.read((byte) 0x8a);
        Assert.assertEquals("HeadState", monitor.getStateInfo());
        monitor.read((byte) 0x8a);
        Assert.assertEquals("BodyState", monitor.getStateInfo());
        monitor.read((byte) 0x41);
        Assert.assertEquals("BodyState", monitor.getStateInfo());
        monitor.read((byte) 0x43);
        Assert.assertEquals("BodyState", monitor.getStateInfo());
        monitor.read((byte) 0x45);
        Assert.assertEquals("BodyState", monitor.getStateInfo());
        monitor.read((byte) 0xa8);
        Assert.assertEquals("TailState", monitor.getStateInfo());
        monitor.read((byte) 0xa7);
        Assert.assertEquals("IdleState", monitor.getStateInfo());
    }
}
