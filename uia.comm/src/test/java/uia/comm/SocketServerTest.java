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
import uia.comm.my.MyManager;
import uia.comm.protocol.ng.NGProtocol;

/**
 *
 * @author Kyle K. Lin
 *
 */
public class SocketServerTest {

    public SocketServerTest() {
        PropertyConfigurator.configure("log4j.properties");
    }

    @Test
    public void testOnlyOne() throws Exception {
        SocketServer server = create("OnlyOne", 2234, ConnectionStyle.ONLYONE);
        server.start();

        for (int i = 0; i < 4; i++) {
            SocketClient client = new SocketClient(new NGProtocol<SocketDataController>(), new MyManager(), "oo-" + i);
            Assert.assertTrue(client.connect("localhost", 2234));
            Thread.sleep(4000);
            Assert.assertEquals(1, server.getClientCount());
        }

        server.stop();
    }

    @Test
    public void testOneEachClient() throws Exception {
        SocketServer server = create("OneEachClient", 2235, ConnectionStyle.ONE_EACH_CLIENT);
        server.start();

        for (int i = 0; i < 4; i++) {
            SocketClient client = new SocketClient(new NGProtocol<SocketDataController>(), new MyManager(), "oec-" + i);
            Assert.assertTrue(client.connect("localhost", 2235));
            Thread.sleep(4000);
            Assert.assertEquals(1, server.getClientCount());
        }

        server.stop();
    }

    @Test
    public void testNormalType() throws Exception {
        SocketServer server = create("Normal", 2236, ConnectionStyle.NORMAL);
        server.start();

        for (int i = 0; i < 4; i++) {
            SocketClient client = new SocketClient(new NGProtocol<SocketDataController>(), new MyManager(), "n-" + i);
            Assert.assertTrue(client.connect("localhost", 2236));
            Thread.sleep(4000);
            Assert.assertEquals(i + 1, server.getClientCount());
        }

        server.stop();
    }

    @Test
    public void testIdle() throws Exception {
        SocketServer server = create("Idle", 2238, ConnectionStyle.NORMAL);
        server.setIdleTime(1500);
        server.start();

        SocketClient client = new SocketClient(new NGProtocol<SocketDataController>(), new MyManager(), "clnt");
        Assert.assertTrue(client.connect("localhost", 2238));
        Thread.sleep(500);
        Assert.assertEquals(1, server.getClientCount());
        Thread.sleep(3500);
        Assert.assertEquals(0, server.getClientCount());

        server.stop();
    }

    private SocketServer create(String name, int port, ConnectionStyle cs) throws Exception {
        final SocketServer server = new SocketServer(
                new NGProtocol<SocketDataController>(),
                port,
                new MyManager(),
                name,
                cs);
        server.addServerListener(new SocketServerListener() {

            @Override
            public void connected(SocketDataController controller) {
            }

            @Override
            public void disconnected(SocketDataController controller) {
            }

        });
        return server;
    }
}
