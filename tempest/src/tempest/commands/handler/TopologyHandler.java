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
import tempest.services.Tuple;
import tempest.services.spout.SpoutService;

import java.net.Inet4Address;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by swapnalekkala on 12/5/15.
 */
public class TopologyHandler implements ResponseCommandExecutor<Topology, String, String> {
    private Socket socket;
    private MembershipService membershipService;
    private SpoutService spoutService;
    private LinkedBlockingQueue<Tuple> queue;


    public boolean canHandle(Message.Type type) {
        return type == Topology.type;
    }

    public TopologyHandler() {
    }

    public TopologyHandler(MembershipService membershipService, Logger logger,LinkedBlockingQueue<Tuple> queue) {
        this.membershipService = membershipService;
        this.spoutService = new SpoutService(membershipService, logger);
        this.queue = queue;
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
            String introducer = "localhost:4444";
            Membership.Member member = Membership.Member.newBuilder()
                    .setHost(introducer.split(":")[0])
                    .setPort(Integer.parseInt(introducer.split(":")[1]))
                    .build();

            String introducer1 = "localhost:4445";
            Membership.Member member1 = Membership.Member.newBuilder()
                    .setHost(introducer1.split(":")[0])
                    .setPort(Integer.parseInt(introducer1.split(":")[1]))
                    .build();

            Topology topology = (Topology) command;

            Bolt bolt = new Bolt();
            bolt.setSendTupleTo(member1);
            bolt.setBoltType(Command.Bolt.BoltType.FILTERBOLT);
            spoutService.setSpout(topology.getSpout());

            spoutService.start(member, bolt);

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

      /*  for (int i = 0; i < topology.getBoltList().size(); i++) {
            topology.getBoltList().get(i).setSendTupleTo(IdMemberMap.get(topology.getBoltList().get(i).getId()));

        }*/



        topology.getSpout().setSendTo(spoutTo);
        spoutService.setSpout(topology.getSpout());

        for(int i = 0; i < topology.getBoltList().size(); i++) {
            Bolt bolt = topology.getBoltList().get(i);
            bolt.setSendTupleTo(IdMemberMap.get(spoutToBoltToBoltID.get(topology.getBoltList().get(i).getId())));
            spoutService.start(IdMemberMap.get(topology.getBoltList().get(i).getId()), bolt);
        }



        this.queue = spoutService.tuplesFromFile(queue);
    }

}
