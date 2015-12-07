package tempest.services.bolt;

import tempest.commands.Response;
import tempest.commands.command.Bolt;
import tempest.commands.handler.BoltHandler;
import tempest.commands.interfaces.ResponseCommand;
import tempest.commands.interfaces.ResponseCommandExecutor;
import tempest.interfaces.Logger;
import tempest.networking.TcpClientResponseCommandExecutor;
import tempest.protos.Command;
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

    public OutputCollector() {
    }

    public OutputCollector(Membership.Member member) {
        this.member = member;
    }

    public void add(Tuple tuple) {
        this.queue.add(tuple);
    }

    public LinkedBlockingQueue<Tuple> getQueue() {
        return this.queue;
    }

    public Runnable emit() {
        return new Runnable() {
            @Override
            public void run() {
                Response<String> response = null;
                boolean run = true;
                while (run) {
                    response = spoutTo(member);
                    if (response.getResponse().equals("ok")) run = false;
                }
            }
        };
    }


    public Response spoutTo(Membership.Member member) {
        Bolt bolt = new Bolt();
        bolt.setTuplesQueue(queue);
        return createResponseExecutor(member, bolt).executeSendTupleFromQueue();
    }

    private <TRequest, TResponse> TcpClientResponseCommandExecutor<ResponseCommand<TRequest, TResponse>, TRequest, TResponse>
    createResponseExecutor(Membership.Member member, ResponseCommand<TRequest, TResponse> command) {
        ResponseCommandExecutor commandHandler = new BoltHandler();
        return new TcpClientResponseCommandExecutor<>(member, command, commandHandler, logger);
    }
}
