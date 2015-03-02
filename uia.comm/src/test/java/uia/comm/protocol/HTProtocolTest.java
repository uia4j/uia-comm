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

import uia.comm.protocol.ht.HTProtocol;

public class HTProtocolTest implements ProtocolEventHandler<Object> {

	private final HTProtocol<Object> protocol;

	public HTProtocolTest() {
		this.protocol = new HTProtocol<Object>(
		        new byte[] { (byte) 0x8a, (byte) 0x8a },
		        new byte[] { (byte) 0xa8, (byte) 0xa8 });
		this.protocol.addMessageHandler(this);
	}

	@Test
	public void testNormal() {

		ProtocolMonitor<Object> monitor = this.protocol.createMonitor("abc");
		monitor.read((byte) 0x8a);
		monitor.read((byte) 0x8a);
		monitor.read((byte) 0x41);
		monitor.read((byte) 0x43);
		monitor.read((byte) 0x45);
		monitor.read((byte) 0xa8);
		monitor.read((byte) 0xa8);
	}

	@Test
	public void testEx1() {

		ProtocolMonitor<Object> monitor = this.protocol.createMonitor("abc");
		monitor.read((byte) 0x8a);
		monitor.read((byte) 0x8a);
		monitor.read((byte) 0x41);
		monitor.read((byte) 0x43);

		monitor.read((byte) 0x8a);
		monitor.read((byte) 0x8a);
		monitor.read((byte) 0x45);
		monitor.read((byte) 0xa8);
		monitor.read((byte) 0xa8);
	}

	@Test
	public void testEx2() {

		ProtocolMonitor<Object> monitor = this.protocol.createMonitor("abc");
		monitor.read((byte) 0x8a);
		monitor.read((byte) 0x8a);
		monitor.read((byte) 0x41);
		monitor.read((byte) 0xa8);

		monitor.read((byte) 0x8a);
		monitor.read((byte) 0x8a);
		monitor.read((byte) 0x43);
		monitor.read((byte) 0x45);
		monitor.read((byte) 0xa8);
		monitor.read((byte) 0xa8);
	}

	@Override
	public void messageReceived(ProtocolMonitor<Object> monitor, ProtocolEventArgs args) {
		System.out.println("r:" + new String(args.getData()));
	}

	@Override
	public void messageError(ProtocolMonitor<Object> monitor, ProtocolEventArgs args) {
		System.out.println("e:" + new String(args.getData()));
	}
}
