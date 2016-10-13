/*******************************************************************************
 * * Copyright (c) 2014, UIA * All rights reserved. * Redistribution and use in source and binary forms, with or without * modification, are permitted provided that the following conditions are met: * * * Redistributions of source code must retain
 * the above copyright * notice, this list of conditions and the following disclaimer. * * Redistributions in binary form must reproduce the above copyright * notice, this list of conditions and the following disclaimer in the * documentation and/or
 * other materials provided with the distribution. * * Neither the name of the {company name} nor the * names of its contributors may be used to endorse or promote products * derived from this software without specific prior written permission. * *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS "AS IS" AND ANY * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE * DISCLAIMED. IN NO
 * EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; * LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS * SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package uia.comm;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;

import uia.comm.SocketServer.ConnectionStyle;
import uia.comm.my.MyManager;
import uia.comm.protocol.ng.NGProtocol;

public class SocketServerTest {

    public static Logger logger = Logger.getLogger(SocketServerTest.class);

    private final NGProtocol<SocketDataController> serverProtocol;

    private final NGProtocol<SocketDataController> clientProtocol;

    private final MyManager manager;

    private SocketDataController controller;

    public SocketServerTest() {
        PropertyConfigurator.configure("log4j.properties");

        this.manager = new MyManager();
        this.serverProtocol = new NGProtocol<SocketDataController>();
        this.clientProtocol = new NGProtocol<SocketDataController>();

    }

    @Test
    public void testCreate() throws Exception {
        SocketClient client = new SocketClient(this.clientProtocol, this.manager, "c1");

        SocketServer server = create(ConnectionStyle.NORMAL);
        System.out.println("svr start:" + server.start());

        Thread.sleep(1000);
        System.out.println("clnt:" + client.connect("localhost", 5953));
        Thread.sleep(1000);
        client.disconnect();

        server.stop();

        server.start();
        System.out.println("svr start:" + server.start());

        Thread.sleep(1000);
        System.out.println("clnt:" + client.connect("localhost", 5953));
        Thread.sleep(1000);
        // client.disconnect();

        server.stop();
    }

    @Test
    public void testOnlyOne() throws Exception {
        SocketServer server = create(ConnectionStyle.ONLYONE);
        System.out.println("svr:" + server.start());

        System.out.println("-- create 16360 connections and keep open state --");
        ArrayList<SocketClient> data = new ArrayList<SocketClient>();
        for (int i = 0; i < 16360; i++) {
            SocketClient client = new SocketClient(this.clientProtocol, this.manager, "c1");
            System.out.println(i + ":" + client.connect("localhost", 5953));
            data.add(client);
        }
        Thread.sleep(2000);

        System.out.println("-- disconnect first 100 connections --");
        for (int i = 0; i < 100; i++) {
            SocketClient old = data.remove(0);
            old.disconnect();
        }
        Thread.sleep(2000);

        System.out.println("-- close and create coneection one by one --");
        for (int i = 0; i < 1000; i++) {
            SocketClient old = data.remove(0);
            old.disconnect();

            SocketClient client = new SocketClient(this.clientProtocol, this.manager, "c1");
            System.out.println(i + ":" + client.connect("localhost", 5953));
            data.add(client);
        }

        server.stop();
    }

    @Test
    public void testOneEachClient() throws Exception {
        SocketServer server = create(ConnectionStyle.ONE_EACH_CLIENT);
        System.out.println("svr:" + server.start());

        ArrayList<SocketClient> data = new ArrayList<SocketClient>();
        for (int i = 0; i < 16360; i++) {
            SocketClient client = new SocketClient(this.clientProtocol, this.manager, "c1");
            System.out.println(i + ":" + client.connect("localhost", 5953));
            data.add(client);
        }
        Thread.sleep(2000);

        System.out.println("--");
        for (int i = 0; i < 100; i++) {
            SocketClient old = data.remove(0);
            old.disconnect();
        }
        Thread.sleep(2000);

        System.out.println("--");
        for (int i = 0; i < 1000; i++) {
            SocketClient old = data.remove(0);
            old.disconnect();

            SocketClient client = new SocketClient(this.clientProtocol, this.manager, "c1");
            System.out.println(i + ":" + client.connect("localhost", 5953));
            data.add(client);
        }

        server.stop();
    }

    @Test
    public void testNormal() throws Exception {
        SocketServer server = create(ConnectionStyle.NORMAL);
        server.start();

        System.out.println("-- create 16200 connections and keep open state --");
        ArrayList<SocketClient> data = new ArrayList<SocketClient>();
        for (int i = 0; i < 16200; i++) {
            SocketClient client = new SocketClient(this.clientProtocol, this.manager, "c1");
            System.out.println(i + ":" + client.connect("localhost", 5953));
            data.add(client);
        }
        Thread.sleep(2000);

        System.out.println("-- disconnect first 100 connections --");
        for (int i = 0; i < 100; i++) {
            SocketClient old = data.remove(0);
            old.disconnect();
        }
        Thread.sleep(2000);

        System.out.println("-- close and create coneection one by one --");
        for (int i = 0; i < 1000; i++) {
            SocketClient old = data.remove(0);
            old.disconnect();

            SocketClient client = new SocketClient(this.clientProtocol, this.manager, "c1");
            System.out.println(i + ":" + client.connect("localhost", 5953));
            data.add(client);
        }

        server.stop();
    }

    @Test
    public void testNormaFlow1() throws Exception {
        SocketServer server = create(ConnectionStyle.ONLYONE);
        server.start();

        for (int i = 0; i < 16200; i++) {
            SocketClient client = new SocketClient(this.clientProtocol, this.manager, "c1");
            System.out.println(i + ":" + client.connect("localhost", 5953));
        }
        Thread.sleep(2000);

        for (int i = 0; i < 200; i++) {
            SocketClient client = new SocketClient(this.clientProtocol, this.manager, "c1");
            System.out.println(i + ":" + client.connect("localhost", 5953));
            client.disconnect();
        }

        server.stop();
    }

    private SocketServer create(ConnectionStyle cs) throws Exception {
        final SocketServer server = new SocketServer(
                this.serverProtocol,
                5953,
                this.manager,
                "TestServer",
                cs);
        server.addServerListener(new SocketServerListener() {

            @Override
            public void connected(SocketDataController controller) {
                if (SocketServerTest.this.controller != null) {
                    server.disconnect(SocketServerTest.this.controller.getName());
                }
                SocketServerTest.this.controller = controller;
            }

            @Override
            public void disconnected(SocketDataController controller) {
            }

        });
        return server;
    }
}
