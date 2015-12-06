package tempest.services.bolt;

import tempest.protos.Command;
import tempest.services.Tuple;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.*;

/**
 * Created by swapnalekkala on 12/1/15.
 */
public class FilterBolt {

    public static final tempest.protos.Command.Bolt.BoltType type = Command.Bolt.BoltType.FILTERBOLT;

    LinkedBlockingQueue queue;
    private static ExecutorService pool = Executors.newFixedThreadPool(7);
    private OutputCollector outputCollector;

    public tempest.protos.Command.Bolt.BoltType getType() {
        return type;
    }

    public FilterBolt(LinkedBlockingQueue queue, OutputCollector outputCollector) {
        this.queue = queue;
        this.outputCollector = outputCollector;
    }

    public void filter() {
        Collection<Callable<Tuple>> callable = new ArrayList<Callable<Tuple>>() {{
            add(new FilterBoltCallable(queue, outputCollector));
            add(new FilterBoltCallable(queue, outputCollector));
            add(new FilterBoltCallable(queue, outputCollector));
            add(new FilterBoltCallable(queue, outputCollector));
            add(new FilterBoltCallable(queue, outputCollector));
            add(new FilterBoltCallable(queue, outputCollector));
            add(new FilterBoltCallable(queue, outputCollector));
        }};

        try {
            pool.invokeAll(callable);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
