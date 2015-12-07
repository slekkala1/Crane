package tempest.services.storm;

import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;

/**
 * Created by swapnalekkala on 12/7/15.
 */
public class PrinterBolt extends BaseBasicBolt {

    int count =0;
    public PrinterBolt() {

    }

    public void execute(Tuple tuple, BasicOutputCollector collector) {
        System.out.println(tuple);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer ofd) {
        ofd.declare(new Fields("ID", "FILENAME", "CHANGE"));
    }
}