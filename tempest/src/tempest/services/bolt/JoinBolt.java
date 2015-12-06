package tempest.services.bolt;

import tempest.services.Tuple;

import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by swapnalekkala on 12/2/15.
 */
public class JoinBolt
        implements Callable {
    LinkedBlockingQueue<Tuple> queue;
    OutputCollector outputCollector;

    public JoinBolt(LinkedBlockingQueue queue, OutputCollector outputCollector) {
        this.queue = queue;
        this.outputCollector = outputCollector;
    }

    public Tuple call() {
        Tuple tuple = null;
        try {
            if(!outputCollector.member.getHost().equals("")) {
                while((tuple = queue.poll(1000, TimeUnit.MILLISECONDS))!=null) {
                //tuple = ;
                    outputCollector.add(tuple);
                }
                //System.out.println(String.join(",", tuple.getStringList()));
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return tuple;
    }
}
