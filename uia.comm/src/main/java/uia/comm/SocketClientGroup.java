/*******************************************************************************
 * * Copyright (c) 2015, UIA
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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 
 * @author Kan
 *
 */
public class SocketClientGroup {

    private final int nThreads;

    private final HashMap<String, SocketClient> clients;

    /**
     * Constructor.
     * 
     * @param nThreads Number of threads.
     */
    public SocketClientGroup(int nThreads) {
        this.nThreads = nThreads;
        this.clients = new HashMap<String, SocketClient>();
    }

    /**
     * Register socket client using its name.
     * 
     * @param client Socket client.
     */
    public void register(SocketClient client) {
        register(client.getName(), client);
    }

    /**
     * Register socket client using its name.
     * 
     * @param name Name.
     * @param client Socket client.
     */
    public void register(String name, SocketClient client) {
        this.clients.put(name, client);
    }

    /**
     * Remove socket client by name.
     * 
     * @param clientName Name.
     */
    public void unregister(String clientName) {
        this.clients.remove(clientName);
    }

    /**
     * Send data to clients.
     * 
     * @param clientsData data of clients.
     * @param txId Transaction id.
     * @param timeout Timeout millisecond.
     * @return Reply data with its client name.
     */
    public Map<String, byte[]> send(
            final HashMap<String, byte[]> dataOfClients,
            final String txId,
            final int timeout) {
        // thread pool
        ExecutorService serv = Executors.newFixedThreadPool(this.nThreads);

        // submit
        HashMap<String, Future<byte[]>> fs = new HashMap<String, Future<byte[]>>();
        for (Map.Entry<String, byte[]> e1 : dataOfClients.entrySet()) {
            String name = e1.getKey();

            final SocketClient client = this.clients.get(name);
            if (client == null) {   // missing socket client
                continue;
            }

            final byte[] data = e1.getValue();
            Future<byte[]> f = serv.submit(new Callable<byte[]>() {

                @Override
                public byte[] call() throws Exception {
                    try {
                        return client.send(data, txId, timeout);
                    }
                    catch (Exception ex) {
                        return null;
                    }
                }

            });
            fs.put(name, f);
        }

        // get from Future
        HashMap<String, byte[]> result = new HashMap<String, byte[]>();
        for (Map.Entry<String, Future<byte[]>> e2 : fs.entrySet()) {
            try {
                result.put(e2.getKey(), e2.getValue().get());
            }
            catch (Exception e) {
                result.put(e2.getKey(), null);
            }
        }
        serv.shutdown();

        return result;
    }

    /**
     * Send same data to all clients.
     * @param data Data.
     * @param txId Transaction id.
     * @param timeout Timeout millisecond.
     * @return Reply data with its client name.
     */
    public Map<String, byte[]> send(final byte[] data, final String txId, final int timeout) {
        // thread pool
        ExecutorService serv = Executors.newFixedThreadPool(this.nThreads);

        // submit
        HashMap<String, Future<byte[]>> fs = new HashMap<String, Future<byte[]>>();
        for (Map.Entry<String, SocketClient> e1 : this.clients.entrySet()) {
            String name = e1.getKey();
            final SocketClient client = e1.getValue();

            Future<byte[]> f = serv.submit(new Callable<byte[]>() {

                @Override
                public byte[] call() throws Exception {
                    try {
                        return client.send(data, txId, timeout);
                    }
                    catch (Exception ex) {
                        return null;
                    }
                }

            });
            fs.put(name, f);
        }

        // get from Future
        HashMap<String, byte[]> result = new HashMap<String, byte[]>();
        for (Map.Entry<String, Future<byte[]>> e2 : fs.entrySet()) {
            try {
                result.put(e2.getKey(), e2.getValue().get());
            }
            catch (Exception e) {
                result.put(e2.getKey(), null);
            }
        }
        serv.shutdown();

        return result;
    }
}
