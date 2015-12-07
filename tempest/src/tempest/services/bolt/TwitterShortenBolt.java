package tempest.services.bolt;

import tempest.protos.Membership;
import tempest.services.Tuple;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by swapnalekkala on 12/2/15.
 */
public class TwitterShortenBolt
        implements Callable {
    LinkedBlockingQueue<Tuple> queue;
    //    OutputCollector outputCollector;
    private List<OutputCollector> outputCollectorList;


    public TwitterShortenBolt(LinkedBlockingQueue<Tuple> queue, List<OutputCollector> outputCollectorList) {
        this.queue = queue;
        this.outputCollectorList = outputCollectorList;
    }

    public Tuple call() {
        Tuple tuple = null;
        try {
            while ((tuple = queue.poll(10000, TimeUnit.MILLISECONDS)) != null) {
                List<String> list = tuple.getStringList();
                List<String> shortened = new ArrayList<String>();
                shortened.add(list.get(0));
                boolean foundText = false;
                boolean foundName = false;
                for (String obj : list) {
                    if (obj.contains("name") && !foundName) {
                        foundName = true;
                        shortened.add(obj);
                    } else if (obj.contains("text") && !foundText) {
                        foundText = true;
                        shortened.add(obj);
                    }
                    if (foundText && foundName) {
                    	break;
                    }
                }
                tuple.setStringList(shortened);
                //Tuple newTuple = new Tuple(shortened);
                if (shortened.size() > 0) {
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
