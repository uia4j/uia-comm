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

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;

import uia.comm.my.MyClientRequest;
import uia.comm.my.MyManager;
import uia.comm.protocol.ht.HTProtocol;
import uia.comm.protocol.ng.NGProtocol;

public class NGSocketTest {

    public static Logger logger = Logger.getLogger(NGSocketTest.class);

    private final NGProtocol<SocketDataController> serverProtocol;

    private final HTProtocol<SocketDataController> clientProtocol;

    private final SocketServer server;

    private final MyManager manager;

    private final MyClientRequest clientRequest;

    public NGSocketTest() throws Exception {
        PropertyConfigurator.configure("log4j.properties");

        this.manager = new MyManager();
        this.clientRequest = new MyClientRequest();

        this.serverProtocol = new NGProtocol<SocketDataController>();
        this.clientProtocol = new HTProtocol<SocketDataController>(
                new byte[] { (byte) 0x8a },
                new byte[] { (byte) 0xa8 });

        this.server = new SocketServer(this.serverProtocol, 5953, this.manager, "TestServer");
        this.server.registerCallin(this.clientRequest);
        this.server.addServerListener(new SocketServerListener() {

            @Override
            public void connected(SocketDataController controller) {
                logger.info(controller.getName() + " connected");
            }

            @Override
            public void disconnected(SocketDataController controller) {
                logger.info(controller.getName() + " disconnected");
            }

        });
    }

    public void before() throws Exception {
        this.server.start();
        Thread.sleep(1000);
    }

    public void after() throws Exception {
        this.server.stop();
    }

    @Test
    public void testInOut() throws Exception {
        int size = 1280184;
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

        before();

        SocketClient client = new SocketClient(this.clientProtocol, this.manager, "c1");
        SocketClientGroup group = new SocketClientGroup(5);
        group.register(client);
        client.connect("localhost", 5953);
        group.send(data, "2", 2000);

        /**
        client.send(
                data,
                this.clientRequest,
                2000);
         */
        // Thread.sleep(3000);

        // close
        client.disconnect();
        Thread.sleep(5000);

        after();
    }

    public void testIdle() throws Exception {
        int size = 200;
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

        before();

        SocketClient client = new SocketClient(this.clientProtocol, this.manager, "c1");
        SocketClientGroup group = new SocketClientGroup(5);
        group.register(client);
        client.connect("localhost", 5953);
        Thread.sleep(10000);
        client.send(data, new MyClientRequest(), 3);
        Thread.sleep(70000);
        System.out.println("done!");
        after();
    }
}
