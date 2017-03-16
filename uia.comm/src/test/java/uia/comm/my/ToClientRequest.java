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
package uia.comm.my;

import org.junit.Assert;

import uia.comm.MessageCallIn;
import uia.comm.MessageCallOut;
import uia.comm.SocketDataController;

/**
 *
 * @author Kyle K. Lin
 *
 */
public class ToClientRequest implements MessageCallIn<SocketDataController>, MessageCallOut {

    @Override
    public String getCmdName() {
        return "ABC";
    }

    @Override
    public String getTxId() {
        return "1";
    }

    @Override
    public void execute(byte[] request, SocketDataController controller) {
        // DEF12
        try {
            Thread.sleep(150);
            boolean r = controller.send(new byte[] { (byte) 0x8a, 0x44, 0x45, 0x46, 0x31, 0x32, (byte) 0xa8 }, 1);
            Assert.assertTrue(r);
        }
        catch (InterruptedException e) {

        }
    }

    @Override
    public void execute(byte[] reply) {
        Assert.assertArrayEquals(
                new byte[] { (byte) 0x8a, 0x44, 0x45, 0x46, 0x31, 0x32, (byte) 0xa8 },
                reply);
    }

    @Override
    public void timeout() {
        Assert.assertTrue(false);
    }

}
