package tempest.commands.handler;

import tempest.commands.Response;
import tempest.commands.command.Bolt;
import tempest.commands.command.Spout;
import tempest.commands.command.Topology;
import tempest.commands.interfaces.ResponseCommand;
import tempest.commands.interfaces.ResponseCommandExecutor;
import tempest.interfaces.Logger;
import tempest.networking.TcpClientResponseCommandExecutor;
import tempest.protos.Command;
import tempest.protos.Command.Message;
import tempest.protos.Membership;
import tempest.services.MembershipService;
import tempest.services.Tuple;
import tempest.services.spout.SpoutService;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
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
    Logger logger;


    public boolean canHandle(Message.Type type) {
        return type == Topology.type;
    }

    public TopologyHandler() {
    }

    public TopologyHandler(MembershipService membershipService, Logger logger, LinkedBlockingQueue<Tuple> queue) {
        this.membershipService = membershipService;
        this.spoutService = new SpoutService(membershipService, logger);
        this.queue = queue;
    }

    public Message serialize(Topology command) {
        Command.Topology.Builder topologyBuilder = Command.Topology.newBuilder();
        if(command.getSpout()!=null) {
            Command.Spout spout = Command.Spout.newBuilder()
                    .setId(command.getSpout().getId())
                    .setSpoutType(command.getSpout().getSpoutType()).build();

            topologyBuilder.setSpout(spout);
        }

        if (command.getBoltList() != null) {
            for (int i = 0; i < command.getBoltList().size(); i++) {
                Command.Bolt.Builder boltBuilder = Command.Bolt.newBuilder()
                        .setId(command.getBoltList().get(i).getId())
                        .setParallelism(command.getBoltList().get(i).getParallelism())
                        .setBoltType(command.getBoltList().get(i).getBoltType())
                        .setReceiveFromID(command.getBoltList().get(i).getReceiveFromID())
                        .setSendTupleToID(command.getBoltList().get(i).getSendTupleToID())
                        ;

                if(command.getBoltList().get(i).getSendTupleTo()!=null) {
                    boltBuilder.setSendTupleTo(command.getBoltList().get(i).getSendTupleTo());

                }

                topologyBuilder.addBolt(i, boltBuilder.build());
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
        if(message.getSpout()!=null) {
            Spout spout = new Spout();
            spout.setSpoutType(message.getTopology().getSpout().getSpoutType());
            spout.setId(message.getTopology().getSpout().getId());
            topology.setSpout(spout);
        }
        List<Bolt> boltList = new ArrayList<>();
        for (int i = 0; i < message.getTopology().getBoltList().size(); i++) {
            Bolt bolt = new Bolt();
            bolt.setBoltType(message.getTopology().getBolt(i).getBoltType());
            bolt.setId(message.getTopology().getBolt(i).getId());
            bolt.setParallelism(message.getTopology().getBolt(i).getParallelism());
            bolt.setReceiveFromID(message.getTopology().getBolt(i).getReceiveFromID());
            if(message.getTopology().getBolt(i).getSendTupleToID() != 0) {
                bolt.setSendTupleToID(message.getTopology().getBolt(i).getSendTupleToID());
            }

            if(message.getTopology().getBolt(i).getSendTupleTo() != null) {
                bolt.setSendTupleTo(message.getTopology().getBolt(i).getSendTupleTo());
            }
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
            } else {
                System.out.println("Writing boltobject to file at" + Inet4Address.getLocalHost().getHostName());
                Bolt bolt =((Topology) command).getBoltList().get(0);
                FileOutputStream fout = new FileOutputStream("boltObject");
                ObjectOutputStream oos = new ObjectOutputStream(fout);
                oos.writeObject(bolt);
                oos.close();
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "ok";
    }

    public void assignMachines(ResponseCommand<String, String> command) {
//        set all members at Spout for Spout and Bolts
        System.out.println("set all members at Spout for Spout and Bolts");

        List<Membership.Member> memberList = new ArrayList<>();
        List<Membership.Member> spoutTo = new ArrayList<>();
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
                System.out.println("Spout to member"+ randomMember.getHost());
            }
        }

        for (int i = 0; i < topology.getBoltList().size(); i++) {
            int sendToId = topology.getBoltList().get(i).getSendTupleToID();
            int boltId = topology.getBoltList().get(i).getId();
            topology.getBoltList().get(i).setSendTupleTo(IdMemberMap.get(sendToId));
            Topology newTopology = new Topology();
            newTopology.setBoltList(new ArrayList<Bolt>());
            newTopology.getBoltList().add(topology.getBoltList().get(i));
            System.out.println("sending topology with bolt to" + IdMemberMap.get(boltId).getHost());
            Response<String> response = createResponseExecutor(IdMemberMap.get(boltId),newTopology).execute();
            assert response.getResponse().equals("ok");
        }

        spoutService.setSpout(topology.getSpout());
        for (Membership.Member member: spoutTo) {
//            Bolt bolt = new Bolt();
            System.out.println("sending tuples to member" + member);
            spoutService.start(member);
        }

        this.queue = spoutService.tuplesFromFile(queue);
    }

    private <TRequest, TResponse> TcpClientResponseCommandExecutor<ResponseCommand<TRequest, TResponse>, TRequest, TResponse>
    createResponseExecutor(Membership.Member member, ResponseCommand<TRequest, TResponse> command) {
        ResponseCommandExecutor commandHandler = new TopologyHandler();
        return new TcpClientResponseCommandExecutor<>(member, command, commandHandler, logger);
    }
}