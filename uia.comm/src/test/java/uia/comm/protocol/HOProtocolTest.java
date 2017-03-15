/*******************************************************************************
 * * Copyright (c) 2014, UIA
 * * All rights reserved.
 * * Redistribution and use in source and binary forms, with or without
 * * modification, are permitted provided that the following conditions are met:
 * *
 * * * Redistributions of source code must retain the above copyright
 * * notice, this list of conditions and the following disclaimer.
 * * * Redistributions in binary form must reproduce the above copyright
 * * notice, this list of conditions and the following disclaimer in the
 * * documentation and/or other materials provided with the distribution.
 * * * Neither the name of the {company name} nor the
 * * names of its contributors may be used to endorse or promote products
 * * derived from this software without specific prior written permission.
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

import org.junit.Assert;
import org.junit.Test;

import uia.comm.protocol.ho.HOProtocol;

public class HOProtocolTest extends AbstractProtocolTest {

    @Test
    public void testNormal1() {
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

		for(int i=0; i<20; i++) {
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
    public void testNormal2() {
        HOProtocol<Object> protocol = new HOProtocol<Object>(
                new byte[] { (byte) 0x41, (byte) 0x42, (byte) 0x43 },
                0);
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
        monitor.readEnd();
        Assert.assertEquals("IdleState", monitor.getStateInfo());
        Assert.assertEquals(6, this.recvArgs.getData().length);

        monitor.read((byte) 0x43);
        Assert.assertEquals("IdleState", monitor.getStateInfo());
    }
}
