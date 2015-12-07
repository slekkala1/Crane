package tempest.services;

import tempest.commands.command.Bolt;
import tempest.commands.command.Spout;
import tempest.protos.Command;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by swapnalekkala on 12/5/15.
 */
public class Topologies {

    public static tempest.commands.command.Topology application1() {
        tempest.commands.command.Topology topology = new tempest.commands.command.Topology();
        tempest.commands.command.Spout spout1 = new tempest.commands.command.Spout();
        spout1.setId(1);
        spout1.setSpoutType(Command.Spout.SpoutType.STOCKDATASPOUT);
//        spout1.setSendTo();

        tempest.commands.command.Bolt bolt1 = new tempest.commands.command.Bolt();
        bolt1.setId(2);
        bolt1.setBoltType(Command.Bolt.BoltType.STOCKFILTERGAINBOLT);
        bolt1.setReceiveFromID(1);
        bolt1.setParallelism(7);
        List<Integer> sendTo = new ArrayList<Integer>();
        sendTo.add(3);
        bolt1.setSendTupleToID(sendTo);

        tempest.commands.command.Bolt bolt2 = new tempest.commands.command.Bolt();
        bolt2.setBoltType(Command.Bolt.BoltType.STOCKFILTERLARGEBOLT);
        bolt2.setId(3);
        bolt2.setReceiveFromID(2);
        bolt2.setParallelism(7);

        List<Bolt> boltList = new ArrayList<Bolt>();
        boltList.add(bolt1);
        boltList.add(bolt2);

        topology.setSpout(spout1);
        topology.setBoltList(boltList);
        return topology;
    }

    public static tempest.commands.command.Topology application2() {
    	tempest.commands.command.Topology topology = new tempest.commands.command.Topology();
        tempest.commands.command.Spout spout1 = new tempest.commands.command.Spout();
        spout1.setId(1);
        spout1.setSpoutType(Command.Spout.SpoutType.TWITTERSPOUT);
//        spout1.setSendTo();

        tempest.commands.command.Bolt bolt1 = new tempest.commands.command.Bolt();
        bolt1.setId(2);
        bolt1.setBoltType(Command.Bolt.BoltType.TWITTERFILTERTRENDINGBOLT);
        bolt1.setReceiveFromID(1);
        List<Integer> sendTo = new ArrayList<Integer>();
        sendTo.add(3);
        bolt1.setSendTupleToID(sendTo);
        bolt1.setParallelism(7);

        tempest.commands.command.Bolt bolt2 = new tempest.commands.command.Bolt();
        bolt2.setBoltType(Command.Bolt.BoltType.TWITTERSHORTENBOLT);
        bolt2.setId(3);
        bolt2.setReceiveFromID(2);
        bolt2.setParallelism(7);

        List<Bolt> boltList = new ArrayList<Bolt>();
        boltList.add(bolt1);
        boltList.add(bolt2);

        topology.setSpout(spout1);
        topology.setBoltList(boltList);
        return topology;
    }

    public static tempest.commands.command.Topology application3() {
        tempest.commands.command.Topology topology = new tempest.commands.command.Topology();

        return topology;
    }
}
