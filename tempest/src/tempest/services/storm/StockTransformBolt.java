package tempest.services.storm;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.TupleImpl;
import backtype.storm.tuple.Values;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by swapnalekkala on 12/7/15.
 */

public class StockTransformBolt
        extends BaseBasicBolt {

    private OutputCollector collector;

    public StockTransformBolt() {

    }

    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
    }

    public void execute(Tuple tuple, BasicOutputCollector collector) {
        ArrayList valueList = new ArrayList<>();

        double open = Double.parseDouble((String) tuple.getValueByField("OPEN"));
        double close = Double.parseDouble((String) tuple.getValueByField("CLOSE"));
        double change = close - open;

        valueList.add(change);

        if (close > open) {
            collector.emit(new Values(tuple.getValueByField("ID"), tuple.getValueByField("FILENAME"), valueList.get(0)));
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("ID", "FILENAME", "CHANGE"));
    }
}
