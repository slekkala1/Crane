package tempest.services;

import tempest.Machine;
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
    private final Machine[] machines;
    private static ExecutorService pool = Executors.newCachedThreadPool();
    private final Logger logger;
    private Membership.MembershipList membershipList;
    private boolean isRunning = true;

    public GossipClient(Membership.MembershipList membershipList, Machine[] machines, Logger logger, int k) {
        this.membershipList = membershipList;
//        this.machines = machines.getRandomMachines(k);
        this.machines = machines;
        this.logger = logger;
    }

    public void run() {
        while (isRunning) {
            try {
                Thread.sleep(500);

                membershipList = MembershipListUtil.updateMembershipList(membershipList);
                //logger.logLine(Logger.INFO, "Updated Membership list " + membershipList.toString());

                Collection<Callable<Integer>> commandExecutors = new ArrayList<>();
                for (Machine machine : machines) {
                    commandExecutors.add(new ClientCommandExecutor<Integer>(machine));
                }
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
    }

    public void stop() {
        isRunning = false;
    }


    class ClientCommandExecutor<Integer> implements Callable<java.lang.Integer> {
        private final Machine server;

        public ClientCommandExecutor(Machine server) {
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

                membershipList.writeDelimitedTo(aOutput);

                byte[] aSendData = aOutput.toByteArray();
                InetAddress aIp = InetAddress.getByName(server.getHostName());// or aReceivePacket.getAddress();

                DatagramPacket aSendPacket = new DatagramPacket(aSendData, aSendData.length, aIp, 9876);
                aClientSocket.send(aSendPacket);

            } catch (IOException e) {
                logger.logLine(Logger.WARNING, "Client socket failed while connecting to " + server + e);
            }
        }
    }

}