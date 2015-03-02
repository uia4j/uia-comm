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
import org.junit.Ignore;
import org.junit.Test;

import uia.comm.my.MyManager;
import uia.comm.protocol.ng.NGProtocol;

public class SocketClientTest {

    public static Logger logger = Logger.getLogger(SocketClientTest.class);

    private final SocketClient socketClient;

    public SocketClientTest() throws Exception {
        PropertyConfigurator.configure("log4j.properties");
        this.socketClient = new SocketClient(
                new NGProtocol<SocketDataController>(),
                new MyManager(),
                "CLIENT");
    }

    @Test
    public void testPing() {
        System.out.println(SocketClient.ping("192.168.0.100", 2000));
    }

    @Test
    public void testConnect() throws Exception {
        System.out.println(this.socketClient.connect("localhost", 1234));
        Thread.sleep(1000);
        System.out.println(this.socketClient.connect("localhost", 1234));
        this.socketClient.disconnect();
        Thread.sleep(1000);
        System.out.println(this.socketClient.connect("localhost", 1234));
        Thread.sleep(1000);
        System.out.println(this.socketClient.connect("localhost", 1234));
        this.socketClient.disconnect();
        Thread.sleep(1000);
        System.out.println(this.socketClient.connect("localhost", 1234));
        Thread.sleep(1000);
        System.out.println(this.socketClient.connect("localhost", 1234));
        this.socketClient.disconnect();
        Thread.sleep(1000);
    }

    @Test
    @Ignore
    public void testConnectPMC() throws Exception {
        long t1 = System.currentTimeMillis();
        System.out.println(this.socketClient.connect("192.168.0.100", 16000));
        long t2 = System.currentTimeMillis();
        System.out.println(t2 - t1);
        this.socketClient.disconnect();
    }

    @Test
    @Ignore
    public void testConnectOnb() throws Exception {
        long t1 = System.currentTimeMillis();
        System.out.println(this.socketClient.connect("222.66.141.10", 6055));
        long t2 = System.currentTimeMillis();
        System.out.println(t2 - t1);
        Thread.sleep(5000);
        this.socketClient.disconnect();
    }
}
