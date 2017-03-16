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
package uia.comm;

import org.apache.log4j.PropertyConfigurator;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uia.comm.my.ToServerRequest;
import uia.comm.my.MyManager;
import uia.comm.my.ToClientRequest;
import uia.comm.protocol.ht.HTProtocol;

public class HTSocketTest {

	private static final int PORT = 3234;
	
	private final HTProtocol<SocketDataController> serverProtocol;

	private final HTProtocol<SocketDataController> clientProtocol;

	private final SocketServer server;

	private final MyManager manager;

	private final ToClientRequest toClientReq;

	private final ToServerRequest toServerReq;

	private String clientId;

	public HTSocketTest() throws Exception {
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

		this.server = new SocketServer(this.serverProtocol, PORT, this.manager, "svr");
		this.server.registerCallin(this.toServerReq);
		this.server.addServerListener(new SocketServerListener() {

			@Override
			public void connected(SocketDataController controller) {
				HTSocketTest.this.clientId = controller.getName();
			}

			@Override
			public void disconnected(SocketDataController controller) {
			}

		});
	}

	@Before
	public void before() throws Exception {
		this.server.start();
        Thread.sleep(500);
	}

	@After
	public void after() throws Exception {
		this.server.stop();
	}

	@Test
	public void testInOut() throws Exception {
		SocketClient client = new SocketClient(this.clientProtocol, this.manager, "c1", PORT + 1);
		client.registerCallin(this.toClientReq);
		client.connect("localhost", PORT);

		// client 2 server
		// ABC2
		boolean s1 = client.send(
		        new byte[] { (byte) 0x8a, 0x41, 0x42, 0x43, 0x32, (byte) 0xa8 },
		        this.toServerReq,
		        1000);
		Assert.assertTrue(s1);

		// server 2 client
		// ABC1
		boolean s2 = this.server.send(
		        this.clientId,
		        new byte[] { (byte) 0x8a, 0x41, 0x42, 0x43, 0x31, (byte) 0xa8 },
		        this.toClientReq,
		        1000);
		Assert.assertTrue(s2);

		// close
		Thread.sleep(500);
		client.disconnect();
	}
}
