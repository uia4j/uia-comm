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

import java.util.Enumeration;

import org.junit.Ignore;
import org.junit.Test;

import uia.comm.protocol.ng.NGProtocol;
import uia.utils.ByteUtils;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

public class RS232Test implements MessageManager {

    @Test
    @Ignore
    public void testListCOM() {
        Enumeration<?> ports = CommPortIdentifier.getPortIdentifiers();
        while (ports.hasMoreElements()) {
            CommPortIdentifier cpIdentifier = (CommPortIdentifier) ports.nextElement();
            System.out.println(cpIdentifier.getName());
        }
    }

    @Test
    @Ignore
    public void testIO() throws Exception {
        NGProtocol<RS232> protocol = new NGProtocol<RS232>();
        RS232 com3 = new RS232(protocol, this, "CMOM3");
        RS232 com4 = new RS232(protocol, this, "CMOM4");
        com4.registerCallin(new MessageCallIn<RS232>() {

            @Override
            public String getCmdName() {
                return "A";
            }

            @Override
            public void execute(byte[] request, RS232 controller) {
                System.out.println("GOT IT:" + ByteUtils.toHexString(request, "-"));
            }

        });

        System.out.println(com3.connect("COM3", 9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE));
        System.out.println(com4.connect("COM4", 9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE));

        com3.send(new byte[] { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08 });

        Thread.sleep(2000);

        com3.disconnect();
        com4.disconnect();
    }

    @Override
    public boolean isCallIn(String cmd) {
        return true;
    }

    @Override
    public String findCmd(byte[] data) {
        return "A";
    }

    @Override
    public String findTx(byte[] data) {
        return "A";
    }

    @Override
    public byte[] decode(byte[] data) {
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
