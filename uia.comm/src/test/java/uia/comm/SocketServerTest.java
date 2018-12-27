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
package uia.comm;

import org.apache.log4j.PropertyConfigurator;
import org.junit.Assert;
import org.junit.Test;

import uia.comm.SocketServer.ConnectionStyle;
import uia.comm.my.ClientManager;
import uia.comm.my.ServerManager;
import uia.comm.my.ServerRequest;
import uia.comm.protocol.ht.HTProtocol;

/**
 *
 * @author Kyle K. Lin
 *
 */
public class SocketServerTest {
	
	private int size = 100000;

    public SocketServerTest() {
        PropertyConfigurator.configure("log4j.properties");
    }

    @Test
    public void testOnlyOne() throws Exception {
        SocketServer server = create("OnlyOne", 2236, ConnectionStyle.ONLYONE);
        server.start();

        for (int i = 0; i < 4; i++) {
            SocketClient client = new SocketClient(
                    new HTProtocol<SocketDataController>("BEGIN_".getBytes(), "_END".getBytes()),
            		new ClientManager(), 
            		"clnt" + i);
            client.setMaxCache(2000000);
            client.registerCallin(new ServerRequest(client.getName(), this.size));
            client.connect("localhost", 2236);
            Thread.sleep(1000);
            Assert.assertEquals(1, server.getClientCount());
            Thread.sleep(5000);
        }

        Thread.sleep(120000);
        server.stop();
    }

    @Test
    public void testOneEachClient() throws Exception {
        SocketServer server = create("OneEachClient", 2236, ConnectionStyle.ONE_EACH_CLIENT);
        server.start();

        for (int i = 0; i < 4; i++) {
            SocketClient client = new SocketClient(
                    new HTProtocol<SocketDataController>("BEGIN_".getBytes(), "_END".getBytes()),
            		new ClientManager(), 
            		"clnt" + i);
            client.setMaxCache(2000000);
            client.registerCallin(new ServerRequest(client.getName(), this.size));
            client.connect("localhost", 2236);
            Thread.sleep(1000);
            Assert.assertEquals(1, server.getClientCount());
            Thread.sleep(5000);
        }

        Thread.sleep(120000);
        server.stop();
    }

    @Test
    public void testNormalType() throws Exception {
        SocketServer server = create("Normal", 2236, ConnectionStyle.NORMAL);
        server.start();

        for (int i = 0; i < 10; i++) {
            SocketClient client = new SocketClient(
                    new HTProtocol<SocketDataController>("BEGIN_".getBytes(), "_END".getBytes()),
            		new ClientManager(), 
            		"clnt" + i);
            client.setMaxCache(2000000);
            client.registerCallin(new ServerRequest(client.getName(), this.size));
            client.connect("localhost", 2236);
            Assert.assertEquals(i + 1, server.getClientCount());
            Thread.sleep(256);
        }

        Thread.sleep(120000);
        server.stop();
        Thread.sleep(2000);
    }

    @Test
    public void testIdle() throws Exception {
        final SocketServer server = new SocketServer(
                new HTProtocol<SocketDataController>("BEGIN_".getBytes(), "_END".getBytes()),
                2236,
                new ServerManager(),
                "Idle1500",
                ConnectionStyle.NORMAL);
        server.setMaxCache(2000000);
        server.setIdleTime(1500);
        server.start();

        SocketClient client = new SocketClient(
                new HTProtocol<SocketDataController>("BEGIN_".getBytes(), "_END".getBytes()),
        		new ClientManager(), 
        		"clnt");
        Assert.assertTrue(client.connect("localhost", 2236));

        Thread.sleep(500);
        Assert.assertEquals(1, server.getClientCount());
        Thread.sleep(3500);
        Assert.assertEquals(0, server.getClientCount());

        server.stop();
    }

    private SocketServer create(String name, int port, ConnectionStyle cs) throws Exception {
        final SocketServer server = new SocketServer(
                new HTProtocol<SocketDataController>("BEGIN_".getBytes(), "_END".getBytes()),
                port,
                new ServerManager(),
                name,
                cs);
        server.setMaxCache(2000000);
        server.addServerListener(new SocketServerListener() {

            @Override
            public void connected(SocketDataController controller) {
            	ServerRequest req = new ServerRequest(controller.getName(), SocketServerTest.this.size);
            	for(int i =0; i<20; i++) {
            		String tx = "" + (i % 10);
	            	try {
	            		byte[] data = req.sampling(tx);
	            		System.out.println(controller.getName() + ", " + tx + "> request: " + data.length);
						server.send(controller.getName(), data, req, 500);
		            	Thread.sleep(1000);
					} catch (Exception e) {
	            		System.out.println(controller.getName() + ", " + tx + "> broken");
					}
            	}
            }

            @Override
            public void disconnected(SocketDataController controller) {
            }

        });
        return server;
    }
}
