package tempest.services.spout;

import tempest.commands.Response;
import tempest.commands.command.*;
import tempest.commands.handler.*;
import tempest.commands.interfaces.*;
import tempest.interfaces.BaseSpout;
import tempest.interfaces.ClientResponseCommandExecutor;
import tempest.interfaces.Logger;
import tempest.networking.TcpClientResponseCommandExecutor;
import tempest.networking.UdpClientResponseCommandExecutor;
import tempest.protos.Membership;
import tempest.services.MembershipService;
import tempest.services.Tuple;
import tempest.services.TupleService;

import java.util.*;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by swapnalekkala on 11/28/15.
 */
public class SpoutService {
    private static ExecutorService pool = Executors.newFixedThreadPool(7);
    LinkedBlockingQueue<Tuple> queue = new LinkedBlockingQueue();
    BaseSpout baseSpout;
    private final MembershipService membershipService;
    private final Logger logger;
    private ResponseCommandExecutor[] responseCommandHandlers;
    private Spout spout;
    Set<Tuple> tupleSet = Collections.synchronizedSet(new HashSet<Tuple>());


    public SpoutService(MembershipService membershipService, Logger logger) {
        this.membershipService = membershipService;
        this.logger = logger;
        this.responseCommandHandlers = new ResponseCommandExecutor[]{new TopologyHandler(), new BoltHandler()};
    }

    public Set<Tuple> getTupleSet() {
        return tupleSet;
    }

    public Spout getSpout() {
        return spout;
    }

    public void setSpout(Spout spout) {
        this.spout = spout;
    }

    public void start(Membership.Member member) {
        System.out.println("Spout type" + this.spout.getSpoutType().toString());
        try {
            if (this.spout.getSpoutType().toString().equals("STOCKDATASPOUT")) {
                baseSpout = new StockDataSpout(queue);
                System.out.println("sending tuples from stockdataspout");
                Thread thread = new Thread(baseSpout.retrieveTuples());
                thread.start();
                thread.join();
            } else if (this.spout.getSpoutType().toString().equals("TWITTERSPOUT")) {
                baseSpout = new TwitterStreamSpout(queue);
                System.out.println("sending tuples from twitterspout");
                Thread thread = new Thread(baseSpout.retrieveTuples());
                thread.start();
                thread.join();
            } else if (this.spout.getSpoutType().toString().equals("BASEBALLSPOUT")) {
                baseSpout = new BaseballDataSpout(queue);
                System.out.println("sending tuples from baseball spout");
                Thread thread = new Thread(baseSpout.retrieveTuples());
                thread.start();
                thread.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        while (!queue.isEmpty()) {
            List<Tuple> tuples = new ArrayList<>();
            queue.drainTo(tuples, 1000000);
            SpoutThread spoutThread = new SpoutThread(tuples, member);

            spoutThread.run();
        }
    }

    class SpoutThread implements Runnable {
        List<Tuple> tuples = new ArrayList<>();
        Membership.Member member;

        public SpoutThread(List<Tuple> tuples, Membership.Member member) {
            this.tuples = tuples;
            this.member = member;
        }

        @Override
        public void run() {
            Response<String> response = null;
            boolean run = true;
            while (run) {
                response = spoutTo(member);
                if (response.getResponse().equals("ok")) run = false;
            }
        }

        public Response spoutTo(Membership.Member member) {
            Bolt bolt = new Bolt();
            System.out.println("sending tuples in spoutservice spoutTo method");
            bolt.setTuplesList(tuples);
            return createResponseExecutor(member, bolt).executeUsingObjectOutputStream();
        }

        private <TRequest, TResponse> TcpClientResponseCommandExecutor<ResponseCommand<TRequest, TResponse>, TRequest, TResponse> createResponseExecutor(Membership.Member member, ResponseCommand<TRequest, TResponse> command) {
            ResponseCommandExecutor commandHandler = null;
            for (ResponseCommandExecutor ch : responseCommandHandlers) {
                if (ch.canHandle(command.getType()))
                    commandHandler = ch;
            }

            return new TcpClientResponseCommandExecutor<>(member, command, commandHandler, logger);
        }
    }
}
