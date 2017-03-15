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
package uia.comm;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import uia.comm.my.ToServerRequest;
import uia.comm.my.MyManager;
import uia.comm.my.ToClientRequest;
import uia.comm.protocol.ht.HTProtocol;

public class SocketTest {

	public static Logger logger = Logger.getLogger(SocketTest.class);

	private final int port = 1234;
	
	private final HTProtocol<SocketDataController> serverProtocol;

	private final HTProtocol<SocketDataController> clientProtocol;

	private final SocketServer server;

	private final MyManager manager;

	private final ToClientRequest toClientReq;

	private final ToServerRequest toServerReq;

	private String clientId;

	public SocketTest() throws Exception {
		PropertyConfigurator.configure("log4j.properties");

		this.manager = new MyManager();
		this.toClientReq = new ToClientRequest();
		this.toServerReq = new ToServerRequest();

		this.serverProtocol = new HTProtocol<SocketDataController>(
		        new byte[] { (byte) 0x8a },
		        new byte[] { (byte) 0xa8 });

		this.clientProtocol = new HTProtocol<SocketDataController>(
		        new byte[] { (byte) 0x8a },
		        new byte[] { (byte) 0xa8 });

		this.server = new SocketServer(this.serverProtocol, this.port, this.manager, "svr");
		this.server.registerCallin(this.toServerReq);
		this.server.addServerListener(new SocketServerListener() {

			@Override
			public void connected(SocketDataController controller) {
				logger.info("clientName: " + controller.getName());
				SocketTest.this.clientId = controller.getName();
			}

			@Override
			public void disconnected(SocketDataController controller) {
			}

		});
	}

	@Before
	public void before() throws Exception {
		this.server.start();
		Thread.sleep(1000);
	}

	@After
	public void after() throws Exception {
		this.server.stop();
	}

	@Test
	public void testPolling() throws Exception {
		SocketClient client = new SocketClient(this.clientProtocol, this.manager, "c2");
		client.connect("localhost", this.port);
		Thread.sleep(10000);
		client.disconnect();
		Thread.sleep(1000);
	}

	@Test
	public void testSend() throws Exception {
		SocketClient client = new SocketClient(this.clientProtocol, this.manager, "c2", 1235);
		client.connect("localhost", this.port);

		Thread.sleep(1000);
		Assert.assertTrue(client.send(new byte[] { 0x01, 0x02, 0x03 }));
		Thread.sleep(1000);
		Assert.assertTrue(client.send(new byte[] { 0x01, 0x02, 0x03 }));
		client.disconnect();

		this.server.disconnect("/127.0.0.1:1235");
	}

	@Test
	public void testRequest() throws Exception {
		SocketClient client = new SocketClient(this.clientProtocol, this.manager, "c1");
		client.registerCallin(this.toClientReq);
		client.connect("localhost", this.port);

		// client 2 server
		boolean s1 = client.send(
		        new byte[] { (byte) 0x8a, 0x41, 0x42, 0x43, 0x32, (byte) 0xa8 },
		        this.toServerReq,
		        1000);
		Assert.assertTrue(s1);

		// server 2 client
		boolean s2 = this.server.send(
		        this.clientId,
		        new byte[] { (byte) 0x8a, 0x41, 0x42, 0x43, 0x31, (byte) 0xa8 },
		        this.toClientReq,
		        1000);

		Assert.assertTrue(s2);

		// close
		Thread.sleep(2000);
		client.disconnect();
	}
}
