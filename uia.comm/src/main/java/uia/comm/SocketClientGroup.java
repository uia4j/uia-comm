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
     * @param dataOfClients data of clients.
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
    public synchronized Map<String, byte[]> send(final byte[] data, final String txId, final int timeout) {
        // thread pool
        ExecutorService serv = Executors.newFixedThreadPool(this.nThreads);

        // submit
        HashMap<String, Future<byte[]>> fs = new HashMap<String, Future<byte[]>>();
        for (final Map.Entry<String, SocketClient> e1 : this.clients.entrySet()) {
            Future<byte[]> f = serv.submit(new Callable<byte[]>() {

                @Override
                public byte[] call() throws Exception {
                    try {
                        final SocketClient client = e1.getValue();
                        final byte[] result = client.send(data, txId, timeout);
                        return result;
                    }
                    catch (Exception ex) {
                        ex.printStackTrace();
                        return null;
                    }
                }

            });
            fs.put(e1.getKey(), f);
        }

        // get from Future
        HashMap<String, byte[]> result = new HashMap<String, byte[]>();
        for (Map.Entry<String, Future<byte[]>> e2 : fs.entrySet()) {
            try {
                byte[] v = e2.getValue().get();
                result.put(e2.getKey(), v);
            }
            catch (Exception e) {
                result.put(e2.getKey(), null);
            }
        }
        serv.shutdown();

        return result;
    }
}
