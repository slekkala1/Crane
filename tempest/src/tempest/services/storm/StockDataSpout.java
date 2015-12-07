package tempest.services.storm;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tempest.interfaces.BaseSpout;
import tempest.services.Tuple;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by swapnalekkala on 12/7/15.
 */
public class StockDataSpout extends BaseRichSpout {

    public static Logger LOG = LoggerFactory.getLogger(StockDataSpout.class);
    boolean _isDistributed;
    SpoutOutputCollector _collector;
    LinkedBlockingQueue<Tuple> queue = new LinkedBlockingQueue<>();


    public StockDataSpout() {
        this(true);
        getQueue();
    }

    public StockDataSpout(boolean isDistributed) {
        this._isDistributed = isDistributed;
    }

    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
        this._collector = collector;
    }

    public void close() {
    }

    public void getQueue() {
        BaseSpout baseSpout = new tempest.services.spout.StockDataSpout(queue);
        System.out.println("sending tuples from stockdataspout");
        Thread thread = new Thread(baseSpout.retrieveTuples());
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void nextTuple() {
        Tuple tuple = new Tuple();
        try {
            if ((tuple = queue.poll(1000, TimeUnit.MILLISECONDS)) != null) {
                this._collector.emit(new Values(tuple.getStringList()));

            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Utils.sleep(100L);
    }

    public void ack(Object msgId) {
    }

    public void fail(Object msgId) {
    }

    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields(new String[]{"word"}));
    }

    public Map<String, Object> getComponentConfiguration() {
        if(!this._isDistributed) {
            HashMap ret = new HashMap();
            ret.put("topology.max.task.parallelism", Integer.valueOf(1));
            return ret;
        } else {
            return null;
        }
    }
}
