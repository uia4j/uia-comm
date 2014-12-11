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
