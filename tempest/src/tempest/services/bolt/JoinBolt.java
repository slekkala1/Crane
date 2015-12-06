package tempest.services.bolt;

import tempest.protos.Command;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by swapnalekkala on 12/1/15.
 */
public class JoinBolt {
    public static final tempest.protos.Command.Bolt.BoltType type = Command.Bolt.BoltType.JOINBOLT;

    LinkedBlockingQueue queue;
    private static ExecutorService pool = Executors.newFixedThreadPool(7);
    private OutputCollector outputCollector;

    public tempest.protos.Command.Bolt.BoltType getType() {
        return type;
    }



}
