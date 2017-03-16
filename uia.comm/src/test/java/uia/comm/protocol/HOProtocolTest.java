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

import uia.comm.protocol.ho.HOProtocol;

/**
 *
 * @author Kyle K. Lin
 *
 */
public class HOProtocolTest extends AbstractProtocolTest {

    @Test
    public void testNormal() {
        HOProtocol<Object> protocol = new HOProtocol<Object>(new byte[] { (byte) 0xee, 0x06, 0x49 }, 25);
        protocol.addMessageHandler(this);

        ProtocolMonitor<Object> monitor = protocol.createMonitor("abc");

        monitor.read((byte) 0x00);
        Assert.assertEquals("IdleState", monitor.getStateInfo());
        monitor.read((byte) 0xee);
        Assert.assertEquals("HeadState", monitor.getStateInfo());
        monitor.read((byte) 0x00);
        Assert.assertEquals("IdleState", monitor.getStateInfo());

        monitor.read((byte) 0xee);
        Assert.assertEquals("HeadState", monitor.getStateInfo());
        monitor.read((byte) 0x06);
        Assert.assertEquals("HeadState", monitor.getStateInfo());
        monitor.read((byte) 0x49);
        Assert.assertEquals("BodyState", monitor.getStateInfo());
        monitor.read((byte) 0x44);
        Assert.assertEquals("BodyState", monitor.getStateInfo());
        monitor.readEnd();	// simulate idle
        Assert.assertEquals("BodyState", monitor.getStateInfo());

        for (int i = 0; i < 20; i++) {
            monitor.read((byte) 0x45);
            Assert.assertEquals("BodyState", monitor.getStateInfo());
        }

        monitor.read((byte) 0x47);
        Assert.assertEquals("IdleState", monitor.getStateInfo());
        Assert.assertEquals(25, this.recvArgs.getData().length);

        monitor.read((byte) 0x47);
        Assert.assertEquals("IdleState", monitor.getStateInfo());
        monitor.read((byte) 0x47);
        Assert.assertEquals("IdleState", monitor.getStateInfo());
    }

    @Test
    public void testAutoEnd() {
        HOProtocol<Object> protocol = new HOProtocol<Object>(
                new byte[] { (byte) 0x41, (byte) 0x42, (byte) 0x43 });  // maxLen = 0
        protocol.addMessageHandler(this);

        ProtocolMonitor<Object> monitor = protocol.createMonitor("abc");

        monitor.read((byte) 0x00);
        Assert.assertEquals("IdleState", monitor.getStateInfo());
        monitor.read((byte) 0x41);
        Assert.assertEquals("HeadState", monitor.getStateInfo());
        monitor.read((byte) 0x42);
        Assert.assertEquals("HeadState", monitor.getStateInfo());

        monitor.read((byte) 0x41);
        Assert.assertEquals("HeadState", monitor.getStateInfo());
        monitor.read((byte) 0x42);
        Assert.assertEquals("HeadState", monitor.getStateInfo());
        monitor.read((byte) 0x43);
        Assert.assertEquals("BodyState", monitor.getStateInfo());
        monitor.read((byte) 0x43);
        Assert.assertEquals("BodyState", monitor.getStateInfo());
        monitor.read((byte) 0x43);
        Assert.assertEquals("BodyState", monitor.getStateInfo());
        monitor.read((byte) 0x43);
        monitor.readEnd();              // finished automatically
        Assert.assertEquals("IdleState", monitor.getStateInfo());
        Assert.assertEquals(6, this.recvArgs.getData().length);

        monitor.read((byte) 0x41);
        Assert.assertEquals("HeadState", monitor.getStateInfo());
        monitor.read((byte) 0x42);
        Assert.assertEquals("HeadState", monitor.getStateInfo());
        monitor.read((byte) 0x43);
        Assert.assertEquals("BodyState", monitor.getStateInfo());
        monitor.read((byte) 0x43);
        Assert.assertEquals("BodyState", monitor.getStateInfo());
        monitor.read((byte) 0x43);
        Assert.assertEquals("BodyState", monitor.getStateInfo());
        monitor.read((byte) 0x43);
        Assert.assertEquals("BodyState", monitor.getStateInfo());
        monitor.read((byte) 0x41);      // finished automatically by head info
        Assert.assertEquals("HeadState", monitor.getStateInfo());
        Assert.assertEquals(6, this.recvArgs.getData().length);
    }

    @Test
    public void testJustHead() {
        HOProtocol<Object> protocol = new HOProtocol<Object>(
                new byte[] { (byte) 0x41, (byte) 0x42, (byte) 0x43 },
                3);
        protocol.addMessageHandler(this);

        ProtocolMonitor<Object> monitor = protocol.createMonitor("abc");

        monitor.read((byte) 0x00);
        Assert.assertEquals("IdleState", monitor.getStateInfo());
        monitor.read((byte) 0x41);
        Assert.assertEquals("HeadState", monitor.getStateInfo());
        monitor.read((byte) 0x42);
        Assert.assertEquals("HeadState", monitor.getStateInfo());
        monitor.read((byte) 0x43);
        Assert.assertEquals("IdleState", monitor.getStateInfo());
        monitor.read((byte) 0x44);
        Assert.assertEquals("IdleState", monitor.getStateInfo());
    }

    @Test
    public void testMixHeadInfo() {
        HOProtocol<Object> protocol = new HOProtocol<Object>(new byte[] { (byte) 0xee, 0x06, 0x49 }, 10);
        protocol.addMessageHandler(this);

        ProtocolMonitor<Object> monitor = protocol.createMonitor("abc");

        monitor.read((byte) 0x00);
        Assert.assertEquals("IdleState", monitor.getStateInfo());
        monitor.read((byte) 0xee);
        Assert.assertEquals("HeadState", monitor.getStateInfo());
        monitor.read((byte) 0x06);
        Assert.assertEquals("HeadState", monitor.getStateInfo());
        monitor.read((byte) 0x49);
        Assert.assertEquals("BodyState", monitor.getStateInfo());
        monitor.read((byte) 0x44);
        Assert.assertEquals("BodyState", monitor.getStateInfo());
        monitor.read((byte) 0xee);  // head info
        Assert.assertEquals("BodyState", monitor.getStateInfo());
        monitor.read((byte) 0x06);  // head info
        Assert.assertEquals("BodyState", monitor.getStateInfo());
        monitor.read((byte) 0x06);
        Assert.assertEquals("BodyState", monitor.getStateInfo());
        monitor.read((byte) 0x06);
        Assert.assertEquals("BodyState", monitor.getStateInfo());
        monitor.read((byte) 0x06);
        Assert.assertEquals("BodyState", monitor.getStateInfo());
        monitor.read((byte) 0x06);
        Assert.assertEquals("IdleState", monitor.getStateInfo());
    }

}
