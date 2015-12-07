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
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by swapnalekkala on 12/7/15.
 */
public class StockFilterGainBolt extends BaseBasicBolt {

    private OutputCollector collector;

    public StockFilterGainBolt() {

    }

    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
    }

    public void execute(Tuple tuple, BasicOutputCollector collector) {
        ArrayList valueList = new ArrayList<>((List) tuple.getValueByField("word"));
        double open = Double.parseDouble((String) valueList.get(3));
        double close = Double.parseDouble((String) valueList.get(6));

        if (close > open) {
            collector.emit(new Values(valueList.get(0),valueList.get(1), valueList.get(2),valueList.get(3), valueList.get(4)
                    ,valueList.get(5), valueList.get(6),valueList.get(7),valueList.get(8)));
        }

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("ID","DATE", "TIME", "OPEN", "HIGH", "LOW", "CLOSE", "VOLUME","FILENAME"));

    }
}
