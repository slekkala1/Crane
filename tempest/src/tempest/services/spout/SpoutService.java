package tempest.services.spout;

import tempest.commands.Response;
import tempest.commands.command.*;
import tempest.commands.interfaces.*;
import tempest.interfaces.ClientResponseCommandExecutor;
import tempest.interfaces.Logger;
import tempest.networking.TcpClientResponseCommandExecutor;
import tempest.networking.UdpClientResponseCommandExecutor;
import tempest.protos.Membership;
import tempest.services.MembershipService;
import tempest.services.Tuple;

import java.util.Random;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by swapnalekkala on 11/28/15.
 */
public class SpoutService {
    private static ExecutorService pool = Executors.newFixedThreadPool(7);

    LinkedBlockingQueue queue = new LinkedBlockingQueue();
    StockDataSpout stockDataSpout = new StockDataSpout(queue);

    private final MembershipService membershipService;
    private final Logger logger;
    private final CommandExecutor[] commandHandlers;
    private final ResponseCommandExecutor[] responseCommandHandlers;

    public SpoutService(MembershipService membershipService, Logger logger, CommandExecutor[] commandHandlers, ResponseCommandExecutor[] responseCommandHandlers) {
        this.membershipService = membershipService;
        this.logger = logger;
        this.commandHandlers = commandHandlers;
        this.responseCommandHandlers = responseCommandHandlers;
    }

    public void start() {
        stockDataSpout.tuplesFromFile1(queue,"xyz").run();
        while(!queue.isEmpty()) {
            List<Tuple> tuples = new ArrayList<>();
            queue.drainTo(tuples, 10000);
            SpoutThread spoutThread = new SpoutThread(tuples,this.membershipService);
            spoutThread.run();
        }
    }

    class SpoutThread implements Runnable {
        List<Tuple> tuples = new ArrayList<>();
        private final MembershipService membershipService;

        public SpoutThread(List<Tuple> tuples, MembershipService membershipService) {
            this.tuples = tuples;
            this.membershipService = membershipService;
        }

        @Override
        public void run() {
            Response<String> response = null;
            boolean run = true;
            while (run) {
//                Membership.MembershipList membershipList = this.membershipService.getMembershipListNoLocal();
//                int index = new Random().nextInt(membershipList.getMemberList().size());
//                Membership.Member member = membershipList.getMemberList().get(index);
                String introducer = "localhost:4444";
                Membership.Member member = Membership.Member.newBuilder()
                        .setHost(introducer.split(":")[0])
                        .setPort(Integer.parseInt(introducer.split(":")[1]))
                        .build();;

                response = spoutTo(member);
                if (response.getResponse().equals("ok")) run = false;
            }
        }

        public Response spoutTo(Membership.Member member) {
            Bolt bolt = new Bolt();
            bolt.setTuplesList(tuples);
//        spout.setRequest(options);
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
