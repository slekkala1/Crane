package tempest.services.bolt;

import tempest.services.Tuple;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.*;

/**
 * Created by swapnalekkala on 12/1/15.
 */
public class FilterBolt {

    LinkedBlockingQueue queue;
    private static ExecutorService pool = Executors.newFixedThreadPool(7);

    public FilterBolt(LinkedBlockingQueue queue) {
        this.queue = queue;
    }

    public void filter() {
        Collection<Callable<Tuple>> callable = new ArrayList<Callable<Tuple>>() {{
            add(new FilterBoltCallable(queue));
            add(new FilterBoltCallable(queue));
            add(new FilterBoltCallable(queue));
            add(new FilterBoltCallable(queue));
            add(new FilterBoltCallable(queue));
            add(new FilterBoltCallable(queue));
            add(new FilterBoltCallable(queue));
        }};


        try {
            pool.invokeAll(callable);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
