package tempest.services;

import tempest.MembershipService;
import tempest.interfaces.Logger;
import tempest.protos.Membership;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

public class GossipClient implements Runnable {
    private static ExecutorService pool = Executors.newCachedThreadPool();
    private final Logger logger;
    private MembershipService membershipService;

    public GossipClient(MembershipService membershipService, Logger logger) {
        this.membershipService = membershipService;
        this.logger = logger;
    }

    public void run() {
        if (membershipService.getMembershipList().getMemberCount() < 2)
            return;
        try {
            membershipService.update();
            Collection<Callable<Integer>> commandExecutors = new ArrayList<>();
            commandExecutors.add(new ClientCommandExecutor<Integer>(membershipService.getRandomMachine()));
            List<Future<Integer>> results;
            results = pool.invokeAll(commandExecutors);
            for (Future<Integer> future : results) {
                try {
                    future.get();
                } catch (ExecutionException e) {
                    logger.logLine(Logger.SEVERE, String.valueOf(e));
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    class ClientCommandExecutor<Integer> implements Callable<java.lang.Integer> {
        private final Membership.Member server;

        public ClientCommandExecutor(Membership.Member server) {
            this.server = server;
        }

        public java.lang.Integer call() {
            execute();
            return 0;
        }

        public void execute() {
            String line;
            long localtimeStamp = System.currentTimeMillis();
            try {
                //logger.logLine(Logger.INFO, "Sending membershipList to GossipServer server on machine " + server.getHostName());

                DatagramSocket aClientSocket = new DatagramSocket();
                ByteArrayOutputStream aOutput = new ByteArrayOutputStream(1024);

                membershipService.getMembershipList().writeDelimitedTo(aOutput);

                byte[] aSendData = aOutput.toByteArray();
                InetAddress aIp = InetAddress.getByName(server.getHost());// or aReceivePacket.getAddress();

                DatagramPacket aSendPacket = new DatagramPacket(aSendData, aSendData.length, aIp, 9876);
                aClientSocket.send(aSendPacket);

            } catch (IOException e) {
                logger.logLine(Logger.WARNING, "Client socket failed while connecting to " + server + e);
            }
        }
    }

}