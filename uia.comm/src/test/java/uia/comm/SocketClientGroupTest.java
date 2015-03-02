/*******************************************************************************
 * * Copyright (c) 2014, UIA
 * * All rights reserved.
 * * Redistribution and use in source and binary forms, with or without
 * * modification, are permitted provided that the following conditions are met:
 * *
 * *     * Redistributions of source code must retain the above copyright
 * *       notice, this list of conditions and the following disclaimer.
 * *     * Redistributions in binary form must reproduce the above copyright
 * *       notice, this list of conditions and the following disclaimer in the
 * *       documentation and/or other materials provided with the distribution.
 * *     * Neither the name of the {company name} nor the
 * *       names of its contributors may be used to endorse or promote products
 * *       derived from this software without specific prior written permission.
 * *
 * * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS "AS IS" AND ANY
 * * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package uia.comm;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;

public class SocketClientGroupTest {

    @Test
    public void testSleep() throws InterruptedException {
        ExecutorService serv = Executors.newFixedThreadPool(4);
        serv.submit(new OneSleep("Kyle1", 2000));
        serv.submit(new OneSleep("Kyle2", 3000));
        serv.submit(new OneSleep("Kyle3", 4000));
        serv.submit(new OneSleep("Kyle4", 5000));
        serv.submit(new OneSleep("Kyle5", 2000));
        serv.submit(new OneSleep("Kyle6", 10));
        serv.submit(new OneSleep("Kyle7", 10));
        serv.submit(new OneSleep("Kyle8", 10));

        Thread.sleep(25000);
        serv.shutdown();
    }

    public static class OneSleep implements Callable<String> {

        private String name;

        private int ms;

        public OneSleep(String name, int ms) {
            this.name = name;
            this.ms = ms;
        }

        @Override
        public String call() throws Exception {
            Thread.sleep(this.ms);
            System.out.println("wakeup " + this.name);
            return this.name;
        }

    }
}
