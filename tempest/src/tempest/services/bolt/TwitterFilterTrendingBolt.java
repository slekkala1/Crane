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
public class TwitterFilterTrendingBolt
        implements Callable {
    LinkedBlockingQueue<Tuple> queue;
//    OutputCollector outputCollector;
    private List<OutputCollector> outputCollectorList;


    public TwitterFilterTrendingBolt(LinkedBlockingQueue<Tuple> queue,  List<OutputCollector> outputCollectorList) {
        this.queue = queue;
        this.outputCollectorList = outputCollectorList;
    }

    public Tuple call() {
        Tuple tuple = null;
        try {
                while((tuple = queue.poll(1000, TimeUnit.MILLISECONDS))!=null) {
                	List<String> list = tuple.getStringList();
                	int favorites = 0;
            		int followers = 0;
            		for (String obj : list) {
            			if (obj.indexOf("favourites_count") != -1) {
                    		favorites = Integer.parseInt(obj.substring(obj.indexOf(':')+1));
            			}
            			else if (obj.indexOf("followers_count") != -1) {
            				followers = Integer.parseInt(obj.substring(obj.indexOf(':') + 1));
            			}
            		}
                	if (favorites > 5*followers) {
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
