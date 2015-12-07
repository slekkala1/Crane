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
    private List<OutputCollector> outputCollectorList;
    double minSize = 1000000;

    public StockFilterLargeBolt(LinkedBlockingQueue<Tuple> queue, List<OutputCollector> outputCollectorList) {
        this.queue = queue;
        this.outputCollectorList = outputCollectorList;
    }

    public Tuple call() {
        Tuple tuple = null;
        try {
            while ((tuple = queue.poll(1000, TimeUnit.MILLISECONDS)) != null) {
                List<String> list = tuple.getStringList();
                double volume = Double.parseDouble(list.get(7));
                if (volume > minSize) {
                    for (int i = 0; i < outputCollectorList.size(); i++) {
                        outputCollectorList.get(i).add(tuple);
                    }
//                		outputCollector.add(tuple);
                    System.out.println("queue size after stock filter large bolt" + outputCollectorList.get(0).getQueue().size());

                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return tuple;
    }
}
