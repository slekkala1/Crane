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
    private List<OutputCollector> outputCollectorList;

    public StockFilterGainBolt() {
    }

    public StockFilterGainBolt(LinkedBlockingQueue<Tuple> queue,  List<OutputCollector> outputCollectorList) {
        this.queue = queue;
        this.outputCollectorList = outputCollectorList;
    }


    public Tuple call() {
        Tuple tuple = null;
        try {
                while((tuple = queue.poll(1000, TimeUnit.MILLISECONDS))!=null) {
                	List<String> list = tuple.getStringList();
                	double open = Double.parseDouble(list.get(3));
                	double close = Double.parseDouble(list.get(6));
                	if (close > open) {
                        //System.out.println("filter gain" + String.join(",", tuple.getStringList()));
                        for (int i = 0; i < outputCollectorList.size(); i++) {
                            outputCollectorList.get(i).add(tuple);
                        }
                	}
                }
            //System.out.println("queue size after stock filter gain bolt" + outputCollectorList.get(0).getQueue().size());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return tuple;
    }
}
