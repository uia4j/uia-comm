/*******************************************************************************
 * Copyright 2017 UIA
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
 
public class HLProtocolTest extends AbstractProtocolTest {

    private final HLProtocol<Object> protocol;

    public HLProtocolTest() {
        this.protocol = new HLProtocol<Object>(
                5,	// lenStartOffset
                1,	// lenEndOffset
                3,	// lenFieldIndex
                2,	// lenFiedlCount
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
    public void testNormal() {
        ProtocolMonitor<Object> monitor = this.protocol.createMonitor("abc");
        
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
    }
}
