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
public class FilterBolt
        implements Callable {
    LinkedBlockingQueue<Tuple> queue;
    List<OutputCollector> outputCollectorList;



    public FilterBolt(LinkedBlockingQueue<Tuple> queue, List<OutputCollector> outputCollectorList) {
        this.queue = queue;
        this.outputCollectorList = outputCollectorList;
    }

    public Tuple call() {
        Tuple tuple = null;
        try {
            if(!outputCollectorList.isEmpty()) {
            	int i = 0;
                while((tuple = queue.poll(1000, TimeUnit.MILLISECONDS))!=null) {
                //tuple = ;
                	OutputCollector outputCollector = outputCollectorList.get(i);
                    outputCollector.add(tuple);
                    i++;
                    if (i == outputCollectorList.size()) {
                    	i = 0;
                    }
                }
                //System.out.println(String.join(",", tuple.getStringList()));
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return tuple;
    }
}
