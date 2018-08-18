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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uia.comm.SocketServer.ConnectionStyle;
import uia.comm.protocol.xml.XMLProtocol;

/**
 *
 * @author Kyle K. Lin
 *
 */
public class XMLSocketTest implements MessageManager, MessageCallOut, MessageCallIn<SocketDataController> {

    private static final int PORT = 4003;

    private final SocketServer server;

    public XMLSocketTest() throws Exception {
        PropertyConfigurator.configure("log4j.properties");

        this.server = new SocketServer(
                new XMLProtocol<SocketDataController>("BBRAUN"),
                PORT,
                this,
                "TestServer1",
                ConnectionStyle.ONE_EACH_CLIENT);
        this.server.registerCallin(this);
        this.server.addServerListener(new SocketServerListener() {

            @Override
            public void connected(SocketDataController controller) {
                System.out.println(controller.getName() + " connected");
            }

            @Override
            public void disconnected(SocketDataController controller) {
                System.out.println(controller.getName() + " disconnected");
            }

        });
    }

    @Before
    public void before() throws Exception {
        this.server.start();
        System.out.println("before");
    }

    @After
    public void after() throws Exception {
        this.server.stop();
        System.out.println("after");
    }

    @Test
    public void testListen() throws Exception {
        try {
            System.out.println("Press  to continue...");
            System.in.read();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getCmdName() {
        return "ABC";
    }

    @Override
    public void execute(byte[] request, SocketDataController controller) {
        System.out.println(controller.getName() + ": " + new String(request));
    }

    @Override
    public String getTxId() {
        return "ABC";
    }

    @Override
    public void execute(byte[] reply) {
        System.out.println("executed");
    }

    @Override
    public void timeout() {
        System.out.println("timeout");
    }

    @Override
    public boolean isCallIn(String cmd) {
        return true;
    }

    @Override
    public String findCmd(byte[] data) {
        return "ABC";
    }

    @Override
    public String findTx(byte[] data) {
        return "ABC";
    }

    @Override
    public byte[] decode(byte[] data) {
        // TODO Auto-generated method stub
        return data;
    }

    @Override
    public byte[] encode(byte[] data) {
        return data;
    }

    @Override
    public boolean validate(byte[] data) {
        return true;
    }
}
