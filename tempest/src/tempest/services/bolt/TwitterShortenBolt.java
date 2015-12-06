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
    OutputCollector outputCollector;

    public TwitterShortenBolt(LinkedBlockingQueue<Tuple> queue, OutputCollector outputCollector) {
        this.queue = queue;
        this.outputCollector = outputCollector;
    }

    public Tuple call() {
        Tuple tuple = null;
        try {
                while((tuple = queue.poll(1000, TimeUnit.MILLISECONDS))!=null) {
                	List<String> list = tuple.getStringList();
                	List<String> shortened = new ArrayList<String>();
            		boolean foundText = false;
            		boolean foundName = false;
            		for (String obj : list) {
            			if (obj.substring(1, 5).equals("name") && !foundName) {
            				foundName = true;
            				shortened.add(obj);
            			}
            			else if (obj.indexOf("text") != -1 && !foundText) {
            				foundText = true;
            				shortened.add(obj);
            			}
            		}
            		tuple.setStringList(shortened);
            		if (shortened.size() > 0) {
            			outputCollector.add(tuple);
            		}
                }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return tuple;
    }
}
