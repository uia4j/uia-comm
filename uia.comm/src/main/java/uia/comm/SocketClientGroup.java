package uia.comm;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
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
            catch (InterruptedException | ExecutionException e) {
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
            catch (InterruptedException | ExecutionException e) {
                result.put(e2.getKey(), null);
            }
        }
        serv.shutdown();

        return result;
    }
}
