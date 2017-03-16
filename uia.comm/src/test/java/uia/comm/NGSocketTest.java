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

import java.util.Map;

import org.apache.log4j.PropertyConfigurator;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uia.comm.SocketServer.ConnectionStyle;
import uia.comm.my.MyManager;
import uia.comm.my.ToServerRequest;
import uia.comm.protocol.ht.HTProtocol;
import uia.comm.protocol.ng.NGProtocol;

/**
 *
 * @author Kyle K. Lin
 *
 */
public class NGSocketTest {

    private static final int PORT = 4234;

    private final MyManager manager;

    private final ToServerRequest toServerReq;

    private final NGProtocol<SocketDataController> serverProtocol;

    private final HTProtocol<SocketDataController> clientProtocol;

    private final SocketServer server;

    public NGSocketTest() throws Exception {
        PropertyConfigurator.configure("log4j.properties");

        this.manager = new MyManager();
        this.toServerReq = new ToServerRequest();

        this.serverProtocol = new NGProtocol<SocketDataController>();
        this.clientProtocol = new HTProtocol<SocketDataController>(
                new byte[] { (byte) 0x8a },
                new byte[] { (byte) 0xa8 });

        this.server = new SocketServer(this.serverProtocol, PORT, this.manager, "TestServer1", ConnectionStyle.NORMAL);
        this.server.registerCallin(this.toServerReq);
        this.server.addServerListener(new SocketServerListener() {

            @Override
            public void connected(SocketDataController controller) {
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
        int size = 1280;
        // ABC2
        byte[] data = new byte[size];
        data[0] = (byte) 0x8a;
        data[1] = 0x41;
        data[2] = 0x42;
        data[3] = 0x43;
        data[4] = 0x32;
        for (int i = 5; i < size; i++) {
            data[i] = (byte) (0x41 + i % 10);
        }
        data[size - 1] = (byte) 0xa8;

        SocketClient client1 = new SocketClient(this.clientProtocol, this.manager, "c1");
        SocketClient client2 = new SocketClient(this.clientProtocol, this.manager, "c2");

        SocketClientGroup group = new SocketClientGroup(4);
        group.register(client1);
        group.register(client2);

        client1.connect("localhost", PORT);
        client2.connect("localhost", PORT);

        Map<String, byte[]> result = group.send(data, "2", 1000);
        for (Map.Entry<String, byte[]> e : result.entrySet()) {
            Assert.assertEquals(7, e.getValue().length);
        }

        // close
        Thread.sleep(500);
        client1.disconnect();
        client2.disconnect();
    }
}
