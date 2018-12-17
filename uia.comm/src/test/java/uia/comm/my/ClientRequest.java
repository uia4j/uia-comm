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
public class ClientRequest implements MessageCallIn<SocketDataController>, MessageCallOut {
	
	private String name;
	
	private String message;
	
	private String tx;
	
	public ClientRequest(String name, int size) {
		this.name = name;
		StringBuilder body = new StringBuilder();
		for(int i=0; i< size; i++) {
			body.append("0");
		}
		this.message = body.append("_END").toString(); 
	}
	
	public byte[] sampling(String tx) {
		this.tx = tx;
		return ("BEGIN_CNTREQ" + this.tx + this.message).getBytes();
	}

	@Override
    public String getCmdName() {
        return "CNTREQ";
    }

    @Override
    public String getTxId() {
        return this.tx;
    }

    @Override
    public void execute(byte[] request, SocketDataController controller) {
		try {
	        String tx = new String(new byte[] { request[12] });
			long t = System.currentTimeMillis() % 1500;
			if(t > 495) {
    			System.out.println(this.name + ", " + tx + "> sleep: " + t);
			}
			Thread.sleep(t);
			
			controller.send(("BEGIN_CNTRSP" + tx + this.message).getBytes(), 1);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    @Override
    public void execute(byte[] reply) {
    	System.out.println(this.name + ", " + this.tx + "> reply:   " + reply.length);
    }

    @Override
    public void timeout() {
    	System.out.println(this.name + ", " + this.tx + "> timeout");
    }

}
