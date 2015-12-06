package tempest.services.bolt;

import tempest.protos.Command;
import tempest.services.Tuple;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by swapnalekkala on 12/1/15.
 */
public class BaseBolt {

    private final tempest.protos.Command.Bolt.BoltType type;

    LinkedBlockingQueue queue;
    private static ExecutorService pool = Executors.newFixedThreadPool(7);
        private List<OutputCollector> outputCollectorList;
   // private OutputCollector outputCollector;

    private final int nThreads;

    public tempest.protos.Command.Bolt.BoltType getType() {
        return type;
    }

        public BaseBolt(LinkedBlockingQueue queue, List<OutputCollector> outputCollectorList, Command.Bolt.BoltType type, int nThreads) {
    //public BaseBolt(LinkedBlockingQueue queue, OutputCollector outputCollector, Command.Bolt.BoltType type, int nThreads) {

        this.queue = queue;
        this.outputCollectorList = outputCollectorList;
        //this.outputCollector = outputCollector;

        this.type = type;
        this.nThreads = nThreads;
    }

    public void filter() {
        Collection<Callable<Tuple>> callable = new ArrayList<Callable<Tuple>>();
        if (type == Command.Bolt.BoltType.FILTERBOLT) {
            for (int i = 0; i < nThreads; i++) {
                callable.add(new FilterBolt(queue, outputCollectorList));
            }
        } else if (type == Command.Bolt.BoltType.JOINBOLT) {
            for (int i = 0; i < nThreads; i++) {

            }
        } else if (type == Command.Bolt.BoltType.TRANSFORMBOLT) {
            for (int i = 0; i < nThreads; i++) {

            }
        }
        try {
            pool.invokeAll(callable);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
