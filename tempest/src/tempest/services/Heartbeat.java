package tempest.services;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Heartbeat {
    private final ScheduledExecutorService scheduler;
    private final GossipClient client;

    public Heartbeat(GossipClient gossipClient) {
        scheduler =  Executors.newScheduledThreadPool(1);
        this.client = gossipClient;
    }

    public void start() {
        scheduler.scheduleAtFixedRate(client, 0, 500, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        scheduler.shutdown();
    }
}
