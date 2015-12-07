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
public class BaseballFilterHRBolt
        implements Callable {
    LinkedBlockingQueue<Tuple> queue;
//    OutputCollector outputCollector;
    private List<OutputCollector> outputCollectorList;

    public BaseballFilterHRBolt(LinkedBlockingQueue<Tuple> queue,  List<OutputCollector> outputCollectorList) {
        this.queue = queue;
        this.outputCollectorList = outputCollectorList;
    }

    public Tuple call() {
        Tuple tuple = null;
        try {
                while((tuple = queue.poll(1000, TimeUnit.MILLISECONDS))!=null) {
                	List<String> list = tuple.getStringList();
                	int hr = Integer.parseInt(list.get(8));
                	if (hr >= 10) {
                        for (int i = 0; i < outputCollectorList.size(); i++) {
                            outputCollectorList.get(i).add(tuple);
                        }
                	}
                }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return tuple;
    }
}
