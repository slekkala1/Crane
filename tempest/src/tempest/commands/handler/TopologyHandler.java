package tempest.commands.handler;

import tempest.commands.command.Bolt;
import tempest.commands.command.Spout;
import tempest.commands.command.Topology;
import tempest.commands.interfaces.ResponseCommand;
import tempest.commands.interfaces.ResponseCommandExecutor;
import tempest.interfaces.Logger;
import tempest.protos.Command;
import tempest.protos.Command.Message;
import tempest.protos.Membership;
import tempest.services.MembershipService;
import tempest.services.spout.SpoutService;

import java.net.Inet4Address;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;

/**
 * Created by swapnalekkala on 12/5/15.
 */
public class TopologyHandler implements ResponseCommandExecutor<Topology, String, String> {
    private Socket socket;
    private MembershipService membershipService;
    private SpoutService spoutService;


    public boolean canHandle(Message.Type type) {
        return type == Topology.type;
    }

    public TopologyHandler() {
    }

    public TopologyHandler(MembershipService membershipService, Logger logger) {
        this.membershipService = membershipService;
        this.spoutService = new SpoutService(membershipService, logger);
    }

    public Message serialize(Topology command) {
        Command.Topology.Builder topologyBuilder = Command.Topology.newBuilder();
        Command.Spout spout = Command.Spout.newBuilder()
                .setId(command.getSpout().getId())
                .setSpoutType(command.getSpout().getSpoutType()).build();

        topologyBuilder.setSpout(spout);

        if (command.getBoltList() != null) {
            for (int i = 0; i < command.getBoltList().size(); i++) {
                Command.Bolt bolt = Command.Bolt.newBuilder()
                        .setId(command.getBoltList().get(i).getId())
                        .setParallelism(command.getBoltList().get(i).getParallelism())
                        .setBoltType(command.getBoltList().get(i).getBoltType())
                        .setReceiveFromID(command.getBoltList().get(i).getReceiveFromID())
                        .setSendTupleToID(command.getBoltList().get(i).getSendTupleToID()).build();

                topologyBuilder.addBolt(i, bolt);
            }
        }
        if (command.getResponse() != null)
            topologyBuilder.setResponse(command.getResponse());
        Message message = Message.newBuilder()
                .setType(Message.Type.TOPOLOGY)
                .setTopology(topologyBuilder)
                .build();
        return message;
    }

    public Topology deserialize(Message message) {
        Topology topology = new Topology();
        Spout spout = new Spout();
        spout.setSpoutType(message.getTopology().getSpout().getSpoutType());
        spout.setId(message.getTopology().getSpout().getId());
        topology.setSpout(spout);

        List<Bolt> boltList = new ArrayList<>();
        for (int i = 0; i < message.getTopology().getBoltList().size(); i++) {
            Bolt bolt = new Bolt();
            bolt.setBoltType(message.getTopology().getBolt(i).getBoltType());
            bolt.setId(message.getTopology().getBolt(i).getId());
            boltList.add(bolt);
        }
        topology.setBoltList(boltList);


        if (message.hasTopology() && message.getTopology().hasResponse())
            topology.setResponse(message.getTopology().getResponse());
        return topology;
    }

    public synchronized Membership.Member getRandomMachineNoLocal() {
        Membership.MembershipList membershipList = this.membershipService.getMembershipListNoLocal();
        int index = new Random().nextInt(membershipList.getMemberList().size());
        Membership.Member randomMachine = membershipList.getMemberList().get(index);
        return randomMachine;
    }


    public String execute(Socket socket, ResponseCommand<String, String> command) {
        try {
            if (Inet4Address.getLocalHost().getHostName().equals("fa15-cs425-g03-01.cs.illinois.edu")) {
                assignMachines(command);
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return "ok";
    }

    public void assignMachines(ResponseCommand<String, String> command) {
        //set all members at Spout for Spout and Bolts
        List<Membership.Member> memberList = new ArrayList<>();
        List<Membership.Member> spoutTo = new ArrayList<>();
        Map<Integer, Integer> spoutToBoltToBoltID = new HashMap<>();
        Map<Integer, Membership.Member> IdMemberMap = new HashMap<>();

        Topology topology = (Topology) command;
        for (int i = 0; i < topology.getBoltList().size(); i++) {

            Membership.Member randomMember = getRandomMachineNoLocal();
            while (memberList.contains(randomMember)) {
                randomMember = getRandomMachineNoLocal();
            }
            memberList.add(randomMember);
            IdMemberMap.put(topology.getBoltList().get(i).getId(), randomMember);
            if (topology.getBoltList().get(i).getReceiveFromID() == 1) {
                spoutTo.add(randomMember);
                spoutToBoltToBoltID.put(topology.getBoltList().get(i).getId(), topology.getBoltList().get(i).getSendTupleToID());
            }
        }

        topology.getSpout().setSendTo(spoutTo);
        spoutService.setSpout(topology.getSpout());

        for (Map.Entry<Integer, Integer> entry : spoutToBoltToBoltID.entrySet()) {
            Bolt bolt = new Bolt();
            bolt.setSendTupleTo(IdMemberMap.get(entry.getValue()));
            spoutService.start(IdMemberMap.get(entry.getKey()), bolt);
        }
    }
}
