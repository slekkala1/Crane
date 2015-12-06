package tempest.services.bolt;

import tempest.commands.Response;
import tempest.commands.command.Bolt;
import tempest.commands.handler.BoltHandler;
import tempest.commands.interfaces.ResponseCommand;
import tempest.commands.interfaces.ResponseCommandExecutor;
import tempest.interfaces.Logger;
import tempest.networking.TcpClientResponseCommandExecutor;
import tempest.protos.Membership;
import tempest.services.Tuple;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by swapnalekkala on 11/28/15.
 */
public class OutputCollector {

    LinkedBlockingQueue<Tuple> queue = new LinkedBlockingQueue<Tuple>();
    Logger logger;
    Membership.Member member;

    public OutputCollector(Membership.Member member) {
        this.member = member;
    }

    public void add(Tuple tuple) {
        this.queue.add(tuple);
    }

    public Runnable emit() {
        return new Runnable() {
            @Override
            public void run() {
                Response<String> response = null;
                boolean run = true;
                while (run) {
//                Membership.MembershipList membershipList = this.membershipService.getMembershipListNoLocal();
//                int index = new Random().nextInt(membershipList.getMemberList().size());
//                Membership.Member member = membershipList.getMemberList().get(index);
                    String introducer = "localhost:4445";
                    Membership.Member member = Membership.Member.newBuilder()
                            .setHost(introducer.split(":")[0])
                            .setPort(Integer.parseInt(introducer.split(":")[1]))
                            .build();
                    ;

                    response = spoutTo(member);
                    if (response.getResponse().equals("ok")) run = false;
                }
            }
        };
    }


    public Response spoutTo(Membership.Member member) {
        Bolt bolt = new Bolt();
        //bolt.setTuplesList(tuples);
//        String introducer = "localhost:4445";
//
//        Membership.Member member1 = Membership.Member.newBuilder()
//                .setHost(introducer.split(":")[0])
//                .setPort(Integer.parseInt(introducer.split(":")[1]))
//                .build();
//        bolt.setSendTupleTo(member1);
        bolt.setTuplesQueue(queue);
//        spout.setRequest(options);
        return createResponseExecutor(member, bolt).executeSendTupleFromQueue();
    }

    private <TRequest, TResponse> TcpClientResponseCommandExecutor<ResponseCommand<TRequest, TResponse>, TRequest, TResponse>
    createResponseExecutor(Membership.Member member, ResponseCommand<TRequest, TResponse> command) {
        ResponseCommandExecutor commandHandler = new BoltHandler();
        return new TcpClientResponseCommandExecutor<>(member, command, commandHandler, logger);
    }
}
