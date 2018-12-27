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

import java.util.Arrays;

import uia.comm.MessageManager;

/**
 *
 * @author Kyle K. Lin
 *
 */
public class ServerManager implements MessageManager {

    @Override
    public boolean isCallIn(String cmd) {
        return "CNTREQ".equals(cmd);
    }

    @Override
    public String findCmd(byte[] data) {
        String cmd = new String(Arrays.copyOfRange(data, 6, 12));
        return cmd;
    }

    @Override
    public String findTx(byte[] data) {
        String tx = new String(Arrays.copyOfRange(data, 12, 13));
        return tx;
    }

    @Override
    public byte[] decode(byte[] data) {
    	System.out.println("server decode");
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
