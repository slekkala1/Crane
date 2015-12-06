package tempest.services.bolt;

import tempest.commands.command.Ack;
import tempest.commands.handler.AckHandler;
import tempest.commands.interfaces.ResponseCommand;
import tempest.commands.interfaces.ResponseCommandExecutor;
import tempest.interfaces.ClientResponseCommandExecutor;
import tempest.interfaces.Logger;
import tempest.networking.TcpClientResponseCommandExecutor;
import tempest.protos.Membership;
import tempest.services.CommandLineExecutor;
import tempest.services.DefaultLogWrapper;
import tempest.services.DefaultLogger;
import tempest.services.Tuple;

import java.io.IOException;
import java.net.Inet4Address;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by swapnalekkala on 12/2/15.
 */
public class FilterBoltCallable
        implements Callable {
    LinkedBlockingQueue<Tuple> queue;
    OutputCollector outputCollector;


    public FilterBoltCallable(LinkedBlockingQueue<Tuple> queue, OutputCollector outputCollector) {
        this.queue = queue;
        this.outputCollector = outputCollector;
    }

    public Tuple call() {
        Tuple tuple = null;
        try {
//            if(!outputCollector.member.getHost().equals("")) {
                while((tuple = queue.poll(1000, TimeUnit.MILLISECONDS))!=null) {
                //filterCondition
                    outputCollector.add(tuple);
                }
//                System.out.println(String.join(",", tuple.getStringList()));
//            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return tuple;
    }

    public void ack(int id) throws IOException {
        Ack ack = new Ack();
        ack.setId(id);
        String introducer = "fa15-cs425-g03-01.cs.illinois.edu:4444";
        Membership.Member member = Membership.Member.newBuilder()
                .setHost(introducer.split(":")[0])
                .setPort(Integer.parseInt(introducer.split(":")[1]))
                .build();
        createResponseExecutor(member, ack).execute();
    }

    private <TRequest, TResponse> ClientResponseCommandExecutor<TResponse> createResponseExecutor
            (Membership.Member member, ResponseCommand<TRequest, TResponse> command) throws IOException {
        ResponseCommandExecutor commandHandler = new AckHandler();
        String logFile = "machine." + Inet4Address.getLocalHost().getHostName() + ".log";
        Logger logger = new DefaultLogger(new CommandLineExecutor(), new DefaultLogWrapper(), logFile, logFile);
        return new TcpClientResponseCommandExecutor<>(member, command, commandHandler, logger);
    }
}
