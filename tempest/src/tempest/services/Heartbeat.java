package tempest.services;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Heartbeat {
    private final ScheduledExecutorService scheduler;
    private final HeartbeatRunner runner;

    public Heartbeat() {
        scheduler =  Executors.newScheduledThreadPool(1);
        runner = new HeartbeatRunner();
        scheduler.scheduleAtFixedRate(runner, 0, 500, TimeUnit.MILLISECONDS);
    }

    class HeartbeatRunner implements Runnable {
        public void run() {

        }
    }
}
