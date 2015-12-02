package tempest.services.bolt;

import tempest.services.Tuple;

import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by swapnalekkala on 12/2/15.
 */
public class FilterBoltCallable
        implements Callable {
    LinkedBlockingQueue queue;

    public FilterBoltCallable(LinkedBlockingQueue queue) {
        this.queue = queue;
    }

    public Tuple call() {
        Tuple tuple = null;
        try {
            tuple = (Tuple) queue.take();
            System.out.println(String.join(",", tuple.getStringList()));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return tuple;
    }
}
