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

import org.junit.Test;

import uia.comm.protocol.ho.HOProtocol;
import uia.utils.ByteUtils;

public class HOProtocolTest implements ProtocolEventHandler<Object> {

	public HOProtocolTest() {
	}

	@Test
	public void testNormal1() {
		HOProtocol<Object> protocol = new HOProtocol<Object>(
		        new byte[] { (byte) 0x41, (byte) 0x42, (byte) 0x43 },
		        5);
		protocol.addMessageHandler(this);

		ProtocolMonitor<Object> monitor = protocol.createMonitor("abc");
		monitor.read((byte) 0x41);
		monitor.read((byte) 0x42);
		monitor.read((byte) 0x43);
		monitor.read((byte) 0x44);
		monitor.readEnd();
		monitor.read((byte) 0x45);

		monitor.read((byte) 0x46);
		monitor.read((byte) 0x47);

		monitor.read((byte) 0x41);
		monitor.read((byte) 0x42);
		monitor.read((byte) 0x43);
		monitor.read((byte) 0x41);
		monitor.read((byte) 0x47);
	}

	@Test
	public void testNormal2() {
		HOProtocol<Object> protocol = new HOProtocol<Object>(
		        new byte[] { (byte) 0x41, (byte) 0x42, (byte) 0x43 },
		        3);
		protocol.addMessageHandler(this);

		ProtocolMonitor<Object> monitor = protocol.createMonitor("abc");
		monitor.read((byte) 0x41);
		monitor.read((byte) 0x42);
		monitor.read((byte) 0x43);
		monitor.read((byte) 0x44);
		monitor.read((byte) 0x45);
	}

	@Test
	public void testNormal3() {
		HOProtocol<Object> protocol = new HOProtocol<Object>(
		        new byte[] { (byte) 0x41, (byte) 0x42, (byte) 0x43 },
		        0);
		protocol.addMessageHandler(this);

		ProtocolMonitor<Object> monitor = protocol.createMonitor("abc");
		monitor.read((byte) 0x41);
		monitor.read((byte) 0x42);
		monitor.read((byte) 0x43);
		monitor.read((byte) 0x44);
		monitor.readEnd();
		monitor.read((byte) 0x45);
		monitor.read((byte) 0x46);
		monitor.read((byte) 0x47);
		monitor.read((byte) 0x41);
		monitor.read((byte) 0x42);
		monitor.read((byte) 0x43);
		monitor.readEnd();
	}

	@Test
	public void testEx1() {
		HOProtocol<Object> protocol = new HOProtocol<Object>(
		        new byte[] { (byte) 0x41, (byte) 0x42, (byte) 0x43 },
		        5);
		protocol.addMessageHandler(this);

		ProtocolMonitor<Object> monitor = protocol.createMonitor("abc");
		monitor.read((byte) 0x41);
		monitor.read((byte) 0x42);

		monitor.read((byte) 0x41);
		monitor.read((byte) 0x42);
		monitor.read((byte) 0x43);
		monitor.read((byte) 0x44);
		monitor.read((byte) 0x45);
	}

	@Test
	public void testEx2() {
		HOProtocol<Object> protocol = new HOProtocol<Object>(
		        new byte[] { (byte) 0x41, (byte) 0x42, (byte) 0x43 },
		        8);
		protocol.addMessageHandler(this);

		ProtocolMonitor<Object> monitor = protocol.createMonitor("abc");
		monitor.read((byte) 0x41);
		monitor.read((byte) 0x42);
		monitor.read((byte) 0x43);
		monitor.read((byte) 0x44);
		monitor.read((byte) 0x41);
		monitor.read((byte) 0x42);
		monitor.read((byte) 0x43);
		monitor.read((byte) 0x44);

		monitor.read((byte) 0x45);
		monitor.read((byte) 0x46);
		monitor.read((byte) 0x47);
		monitor.read((byte) 0x48);
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
