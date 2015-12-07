package tempest.services;

import tempest.commands.command.Topology;
import tempest.commands.handler.TopologyHandler;
import tempest.commands.interfaces.CommandExecutor;
import tempest.commands.interfaces.ResponseCommandExecutor;
import tempest.interfaces.Logger;
import tempest.protos.Membership;
import tempest.sdfs.client.SDFSClient;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by swapnalekkala on 12/6/15.
 */
public class TupleService implements Runnable {
    Set<Integer> ids;
    Set<Tuple> tupleSet = Collections.synchronizedSet(new HashSet<Tuple>());
    List<Membership.Member> memberList;
    private final Logger logger;
    //private final CommandExecutor[] commandHandlers;
    //private final ResponseCommandExecutor[] responseCommandHandlers;
    private final ScheduledExecutorService scheduler;
    private MembershipService membershipService;
    Map<Integer, Membership.Member> idMemberMap;
    Topology topology;

    public TupleService(Logger logger, List<Membership.Member> memberList, Set<Tuple> tupleSet, Set<Integer> ackedIds,
                        MembershipService membershipService, Map<Integer, Membership.Member> idMemberMap,Topology topology) {
        this.logger = logger;
        scheduler = Executors.newScheduledThreadPool(1);
        //this.commandHandlers = commandHandlers;
        //this.responseCommandHandlers = responseCommandHandlers;
        this.memberList = memberList;
        this.tupleSet = tupleSet;
        this.ids = ackedIds;
        this.membershipService = membershipService;
        this.idMemberMap = idMemberMap;
        this.topology = topology;
    }

    public void start() {
        scheduler.scheduleAtFixedRate(this, 0, 5, TimeUnit.SECONDS);
    }

    public void stop() {
        scheduler.shutdown();
    }


    public void run() {

        Membership.Member memberwithMaxID;
        Integer i = 0;

        for (Integer j : idMemberMap.keySet()) {
            if(i==0) i=j;
            if(i<j) {
                i = j;
            }
        }

        memberwithMaxID = idMemberMap.get(i);

        System.out.println("member with maxID " + memberwithMaxID + " " + i);

        System.out.println("membershiplist" + this.membershipService.getMembershipListNoLocal().getMemberList());
        if(!this.membershipService.getMembershipListNoLocal().getMemberList().contains(memberwithMaxID)) {
            System.out.println("Machine with maxId failed");
            new TopologyHandler().assignMachines(topology);
            //replay application
        }

        for (Membership.Member member : memberList) {
            if(!this.membershipService.getMembershipListNoLocal().getMemberList().contains(member)) {
                new TopologyHandler().assignMachines(topology);

                //replay application
            }

        }


        System.out.println("Size of acked Ids" + ids.size());

    }


    //redo logic in Topology handler for the tuple list that is not acknowledged


}
