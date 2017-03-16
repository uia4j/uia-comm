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

import uia.comm.protocol.ng.NGProtocol;

/**
 *
 * @author Kyle K. Lin
 *
 */
public class NGProtocolTest extends AbstractProtocolTest {

    private final NGProtocol<Object> protocol;

    public NGProtocolTest() {
        this.protocol = new NGProtocol<Object>();
        this.protocol.addMessageHandler(this);
    }

    @Test
    public void testNormal() {
        ProtocolMonitor<Object> monitor = this.protocol.createMonitor("abc");
        Assert.assertEquals("IdleState", monitor.getStateInfo());
        monitor.read((byte) 0x43);
        Assert.assertEquals("BodyState", monitor.getStateInfo());
        monitor.read((byte) 0x44);
        Assert.assertEquals("BodyState", monitor.getStateInfo());
        monitor.read((byte) 0x45);
        Assert.assertEquals("BodyState", monitor.getStateInfo());
        monitor.readEnd();
        Assert.assertEquals("IdleState", monitor.getStateInfo());
    }
}
