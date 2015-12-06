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
public class StockFilterGainBolt
        implements Callable {
    LinkedBlockingQueue<Tuple> queue;
//    OutputCollector outputCollector;
    private List<OutputCollector> outputCollectorList;

    public StockFilterGainBolt(LinkedBlockingQueue<Tuple> queue,  List<OutputCollector> outputCollectorList) {
        this.queue = queue;
        this.outputCollectorList = outputCollectorList;
    }

    public Tuple call() {
        Tuple tuple = null;
        try {
                while((tuple = queue.poll(1000, TimeUnit.MILLISECONDS))!=null) {
                	List<String> list = tuple.getStringList();
                	double open = Double.parseDouble(list.get(2));
                	double close = Double.parseDouble(list.get(5));
                	if (close > open) {
                        for (int i = 0; i < outputCollectorList.size(); i++) {
                            outputCollectorList.get(i).add(tuple);
                        }
//                		outputCollector.add(tuple);
                	}
                }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return tuple;
    }
}