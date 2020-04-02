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

import org.junit.Assert;
import org.junit.Test;

import uia.comm.SocketServer.ConnectionStyle;
import uia.comm.my.ClientManager;
import uia.comm.my.ClientRequest;
import uia.comm.my.ServerManager;
import uia.comm.my.ServerRequest;
import uia.comm.protocol.ht.HTProtocol;

/**
 *
 * @author Kyle K. Lin
 *
 */
public class SocketClientTest {
	
	private int size = 100000;

    public SocketClientTest() {
    }

    @Test
    public void testCase1() throws Exception {
        SocketServer server = create("OnlyOne", 2236, ConnectionStyle.ONLYONE);
        server.addServerListener(new SocketServerListener() {

			@Override
			public void connected(SocketDataController controller) {
				System.out.println(controller.getName() + " connected");
			}

			@Override
			public void disconnected(SocketDataController controller) {
				System.out.println(controller.getName() + " disconnected");
			}
        	
        });
        server.start();

        SocketClient client = new SocketClient(
                new HTProtocol<SocketDataController>("BEGIN_".getBytes(), "_END".getBytes()),
        		new ClientManager(), 
        		"clnt");
        client.setMaxCache(2000000);
        client.registerCallin(new ServerRequest(client.getName(), this.size));
        client.connect("localhost", 2236);
        Thread.sleep(1000);
        Assert.assertEquals(1, server.getClientCount());

    	for(int i =0; i<5; i++) {
            ClientRequest req = new ClientRequest("clnt", this.size);
    		String tx = "" + (i % 10);
        	try {
        		byte[] data = req.sampling(tx);
        		System.out.println("clnt, " + tx + "> request: " + data.length);
				client.send(data, req, 500);
			} catch (Exception e) {
        		System.out.println("clnt, " + tx + "> broken");
			}
    	}

    	Thread.sleep(4000);
    	
    	server.stop();
    }


    @Test
    public void testCase2() throws Exception {
        SocketServer server = create("OnlyOne", 2236, ConnectionStyle.ONLYONE);
        server.addServerListener(new SocketServerListener() {

			@Override
			public void connected(SocketDataController controller) {
				System.out.println(controller.getName() + " connected");
			}

			@Override
			public void disconnected(SocketDataController controller) {
				System.out.println(controller.getName() + " disconnected");
			}
        	
        });
        server.start();

        SocketClient client = new SocketClient(
                new HTProtocol<SocketDataController>("BEGIN_".getBytes(), "_END".getBytes()),
        		new ClientManager(), 
        		"clnt");
        client.setMaxCache(2000000);
        client.connect("localhost", 2236);
        Thread.sleep(1000);
        Assert.assertEquals(1, server.getClientCount());

        // normal
    	for(int i =0; i<3; i++) {
            ClientRequest req = new ClientRequest("clnt", this.size);
    		String tx = "" + (i % 10);
        	try {
        		byte[] data = req.sampling(tx);
        		System.out.println("clnt, " + tx + "> request: " + data.length);
				client.send(data, req, 1500);
				Thread.sleep(1000);
			} catch (Exception e) {
        		System.out.println("clnt, " + tx + "> broken");
			}
    	}

    	Thread.sleep(18000);
    	
    	// disconnected already 
    	for(int i =0; i<3; i++) {
            ClientRequest req = new ClientRequest("clnt", this.size);
    		String tx = "" + (i % 10);
        	try {
        		byte[] data = req.sampling(tx);
        		System.out.println("clnt, " + tx + "> request: " + data.length);
				client.send(data, req, 1500);
				Thread.sleep(1000);
			} catch (Exception e) {
        		System.out.println("clnt, " + tx + "> broken");
			}
    	}
    	
    	server.stop();
    }


    @Test
    public void testCase3() throws Exception {
        SocketServer server = create("OnlyOne", 2236, ConnectionStyle.ONLYONE);
        server.addServerListener(new SocketServerListener() {

			@Override
			public void connected(SocketDataController controller) {
				System.out.println(controller.getName() + " connected");
			}

			@Override
			public void disconnected(SocketDataController controller) {
				System.out.println(controller.getName() + " disconnected");
			}
        	
        });
        server.start();

        SocketClient client = new SocketClient(
                new HTProtocol<SocketDataController>("BEGIN_".getBytes(), "_END".getBytes()),
        		new ClientManager(), 
        		"clnt");
        client.setMaxCache(2000000);
        client.connect("localhost", 2236);
        Thread.sleep(1000);
        Assert.assertEquals(1, server.getClientCount());

        // normal
    	for(int i =0; i<3; i++) {
            ClientRequest req = new ClientRequest("clnt", this.size);
    		String tx = "" + (i % 10);
        	try {
        		byte[] data = req.sampling(tx);
        		System.out.println("clnt, " + tx + "> request: " + data.length);
				client.send(data, req, 1500);
				Thread.sleep(1000);
			} catch (Exception e) {
        		System.out.println("clnt, " + tx + "> broken");
			}
    	}
    	Thread.sleep(2000);
    	
    	// server stop 
    	server.stop();
    	
    	Thread.sleep(1000);
    	for(int i =0; i<2; i++) {
            ClientRequest req = new ClientRequest("clnt", this.size);
    		String tx = "" + (i % 10);
        	try {
        		byte[] data = req.sampling(tx);
        		System.out.println("clnt, " + tx + "> request: " + data.length);
				client.send(data, req, 1500);
				Thread.sleep(1000);
			} catch (Exception e) {
        		System.out.println("clnt, " + tx + "> broken");
			}
    	}
    }

    private SocketServer create(String name, int port, ConnectionStyle cs) throws Exception {
        final SocketServer server = new SocketServer(
                new HTProtocol<SocketDataController>("BEGIN_".getBytes(), "_END".getBytes()),
                port,
                new ServerManager(),
                name,
                cs);
        server.setIdleTime(5000);
        server.registerCallin(new ClientRequest(name, this.size));
        server.setMaxCache(2000000);
        return server;
    }
}
