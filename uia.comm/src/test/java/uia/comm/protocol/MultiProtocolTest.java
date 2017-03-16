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

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

import uia.comm.protocol.ho.HOProtocol;
import uia.comm.protocol.ht.HTProtocol;

/**
 *
 * @author Kyle K. Lin
 *
 */
public class MultiProtocolTest extends AbstractProtocolTest {

    private final MultiProtocol<Object> protocol;

    public MultiProtocolTest() {
        HOProtocol<MultiProtocolMonitor<Object>> p1 = new HOProtocol<MultiProtocolMonitor<Object>>(
                new byte[] { (byte) 0x41, (byte) 0x42, (byte) 0x43 },
                6);

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

        Assert.assertEquals("IdleState", monitor.getStateInfo());
        monitor.read((byte) 0x41);
        Assert.assertEquals("RunningState", monitor.getStateInfo());
        monitor.read((byte) 0x42);
        Assert.assertEquals("RunningState", monitor.getStateInfo());
        monitor.read((byte) 0x43);
        Assert.assertEquals("RunningState", monitor.getStateInfo());
        monitor.read((byte) 0x44);
        Assert.assertEquals("RunningState", monitor.getStateInfo());
        monitor.read((byte) 0x44);
        Assert.assertEquals("RunningState", monitor.getStateInfo());
        monitor.read((byte) 0x45);
        Assert.assertEquals("IdleState", monitor.getStateInfo());

        monitor.read((byte) 0x8a);
        Assert.assertEquals("RunningState", monitor.getStateInfo());
        monitor.read((byte) 0x8a);
        Assert.assertEquals("RunningState", monitor.getStateInfo());
        monitor.read((byte) 0x44);
        Assert.assertEquals("RunningState", monitor.getStateInfo());
        monitor.read((byte) 0x45);
        Assert.assertEquals("RunningState", monitor.getStateInfo());
        monitor.read((byte) 0x46);
        Assert.assertEquals("RunningState", monitor.getStateInfo());
        monitor.read((byte) 0xa8);
        Assert.assertEquals("RunningState", monitor.getStateInfo());
        monitor.read((byte) 0xa8);
        Assert.assertEquals("IdleState", monitor.getStateInfo());
    }

    @Test
    public void testEx1() {
        ProtocolMonitor<Object> monitor = this.protocol.createMonitor("abc");

        Assert.assertEquals("IdleState", monitor.getStateInfo());
        monitor.read((byte) 0x8a);
        Assert.assertEquals("RunningState", monitor.getStateInfo());
        monitor.read((byte) 0x8a);
        Assert.assertEquals("RunningState", monitor.getStateInfo());

        monitor.read((byte) 0x41);
        Assert.assertEquals("RunningState", monitor.getStateInfo());
        monitor.read((byte) 0x42);
        Assert.assertEquals("RunningState", monitor.getStateInfo());
        monitor.read((byte) 0x43);
        Assert.assertEquals("RunningState", monitor.getStateInfo());
        monitor.read((byte) 0x44);
        Assert.assertEquals("RunningState", monitor.getStateInfo());
        monitor.read((byte) 0x44);
        Assert.assertEquals("RunningState", monitor.getStateInfo());
        monitor.read((byte) 0x45);
        Assert.assertEquals("IdleState", monitor.getStateInfo());

        monitor.read((byte) 0x46);
        Assert.assertEquals("IdleState", monitor.getStateInfo());

        monitor.read((byte) 0x8a);
        Assert.assertEquals("RunningState", monitor.getStateInfo());
        monitor.read((byte) 0x8a);
        Assert.assertEquals("RunningState", monitor.getStateInfo());
        monitor.read((byte) 0x47);
        Assert.assertEquals("RunningState", monitor.getStateInfo());
        monitor.read((byte) 0x48);
        Assert.assertEquals("RunningState", monitor.getStateInfo());
        monitor.read((byte) 0xa8);
        Assert.assertEquals("RunningState", monitor.getStateInfo());
        monitor.read((byte) 0xa8);
        Assert.assertEquals("IdleState", monitor.getStateInfo());
    }

    @Test
    public void testEx2() {
        ProtocolMonitor<Object> monitor = this.protocol.createMonitor("abc");

        Assert.assertEquals("IdleState", monitor.getStateInfo());
        monitor.read((byte) 0x41);
        Assert.assertEquals("RunningState", monitor.getStateInfo());
        monitor.read((byte) 0x42);
        Assert.assertEquals("RunningState", monitor.getStateInfo());
        monitor.read((byte) 0x43);
        Assert.assertEquals("RunningState", monitor.getStateInfo());

        monitor.read((byte) 0x44);
        Assert.assertEquals("RunningState", monitor.getStateInfo());
        monitor.read((byte) 0x8a);
        Assert.assertEquals("RunningState", monitor.getStateInfo());
        monitor.read((byte) 0x8a);
        Assert.assertEquals("IdleState", monitor.getStateInfo());

        monitor.read((byte) 0x8a);
        Assert.assertEquals("RunningState", monitor.getStateInfo());
        monitor.read((byte) 0x8a);
        Assert.assertEquals("RunningState", monitor.getStateInfo());

        monitor.read((byte) 0x46);
        Assert.assertEquals("RunningState", monitor.getStateInfo());
        monitor.read((byte) 0x8a);
        Assert.assertEquals("RunningState", monitor.getStateInfo());
        monitor.read((byte) 0x8a);
        Assert.assertEquals("RunningState", monitor.getStateInfo());
        monitor.read((byte) 0x47);
        Assert.assertEquals("RunningState", monitor.getStateInfo());
        monitor.read((byte) 0x48);
        Assert.assertEquals("RunningState", monitor.getStateInfo());
        monitor.read((byte) 0xa8);
        Assert.assertEquals("RunningState", monitor.getStateInfo());
        monitor.read((byte) 0xa8);
        Assert.assertEquals("IdleState", monitor.getStateInfo());
    }
}
