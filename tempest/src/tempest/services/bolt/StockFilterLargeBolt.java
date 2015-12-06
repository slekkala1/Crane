package tempest.services.bolt;

import tempest.protos.Membership;
import tempest.services.Tuple;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by swapnalekkala on 12/2/15.
 */
public class StockFilterLargeBolt
        implements Callable {
    LinkedBlockingQueue<Tuple> queue;
    OutputCollector outputCollector;
    double minSize = 1000000;

    public StockFilterLargeBolt(LinkedBlockingQueue<Tuple> queue, OutputCollector outputCollector) {
        this.queue = queue;
        this.outputCollector = outputCollector;
    }

    public Tuple call() {
        Tuple tuple = null;
        try {
                while((tuple = queue.poll(1000, TimeUnit.MILLISECONDS))!=null) {
                	List<String> list = tuple.getStringList();
                	double volume = Double.parseDouble(list.get(6));
                	if (volume > minSize) {
                		outputCollector.add(tuple);
                	}
                }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return tuple;
    }
}
