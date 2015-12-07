package tempest.services;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.AuthorizationException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.topology.TopologyBuilder;

import java.util.HashMap;
import java.util.Map;

import backtype.storm.utils.Utils;
import tempest.services.storm.PrinterBolt;
import tempest.services.storm.StockDataSpout;
import tempest.services.storm.StockFilterGainBolt;
import tempest.services.storm.StockTransformBolt;

/**
 * Created by swapnalekkala on 12/7/15.
 */
public class TopologyForStorm {

    public static void main(String[] args) throws AlreadyAliveException, InvalidTopologyException {
        TopologyBuilder builder = new TopologyBuilder();

        builder.setSpout("1", new StockDataSpout(), 1);
        builder.setBolt("2", new StockFilterGainBolt(), 7)
        .shuffleGrouping("1");
        builder.setBolt("3", new StockTransformBolt(),7)
                .shuffleGrouping("2");

        //builder.setBolt("4", new PrinterBolt())
          //      .shuffleGrouping("3");

        Map conf = new HashMap();
        conf.put(Config.TOPOLOGY_WORKERS, 3);
        conf.put(Config.TOPOLOGY_MAX_TASK_PARALLELISM, 3);
        conf.put(Config.TOPOLOGY_DEBUG, true);

//        try {
//            StormSubmitter.submitTopology("mytopology", conf, builder.createTopology());
//        } catch (AuthorizationException e) {
//            e.printStackTrace();
//        }
        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology("mytopology", conf, builder.createTopology());
     //   Utils.sleep(100000);
       // cluster.killTopology("test");
        //cluster.shutdown();
    }


}
