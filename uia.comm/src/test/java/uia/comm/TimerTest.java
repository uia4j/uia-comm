package uia.comm;

import java.util.Timer;
import java.util.TimerTask;

public class TimerTest {

    public void test() throws InterruptedException {

        Timer timer1 = new Timer();
        TimerTask task1 = new TimerTask() {

            @Override
            public void run() {
                System.out.println("?");
            }
        };
        timer1.schedule(task1, 2000, 2000);
        Thread.sleep(7000);

        timer1.cancel();
        timer1.purge();

        Timer timer2 = new Timer();
        TimerTask task2 = new TimerTask() {

            @Override
            public void run() {
                System.out.println("ok");
            }
        };
        timer2.schedule(task2, 2000, 2000);
        Thread.sleep(7000);
    }
}
