package tempest.services;

import main.resources.MembershipListProtos;
import tempest.Machine;
import tempest.Machines;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by swapnalekkala on 9/26/15.
 */
public class Daemon {

    private final Logger logger;
    private final int port;
    private ServiceRunner runner;
    private DaemonClient daemonClient;

    private MembershipListProtos.MembershipList membershipList = MembershipListUtil.getNewMembershipList();
    private static ExecutorService pool = Executors.newCachedThreadPool();

    public Daemon(Logger logger, int port) {
        this.logger = logger;
        this.port = port;
    }

    public void start() {
        if (runner != null)
            return;
        runner = new ServiceRunner();
        new Thread(runner).start();
        if (daemonClient !=null)
            return;
        try {
            daemonClient = new DaemonClient(new Machines(),3);//do I need to know which machines are up before sending gossip?
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        new Thread(daemonClient).start();
    }

    public void stop() {
        if (runner == null)
            return;
        runner.stop();
        runner = null;
    }

    class ServiceRunner implements Runnable {
        private boolean isRunning = true;
        private DatagramSocket serverSocket;

        public void run() {
            try {
                DatagramSocket serverSocket = new DatagramSocket(port);

                while (isRunning) {
                    logger.logLine(Logger.INFO, "Started Daemon server on" + InetAddress.getLocalHost().getHostName());
                    byte[] receiveData = new byte[1024];
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    serverSocket.receive(receivePacket);
                    ByteArrayInputStream inputStream = new ByteArrayInputStream(receiveData);
                    MembershipListProtos.MembershipList receivedMembershipList = MembershipListProtos.MembershipList.parseDelimitedFrom(inputStream);
                    MembershipListUtil.mergeMembershipList(receivedMembershipList, membershipList);
                }
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void stop() {
            isRunning = false;
            if (serverSocket == null)
                return;
            serverSocket.close();
        }
    }

    class DaemonClient implements Runnable {

        private final List<Machine> machines;

        public DaemonClient(Machines machines, int k) {
            this.machines = machines.getRandomMachines(k);
        }

        public void run() {
            while (true) {
                try {
                    Thread.sleep(500);
                    MembershipListUtil.updateMembershipList(membershipList);

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
                    DatagramSocket aClientSocket = new DatagramSocket();
                    ByteArrayOutputStream aOutput = new ByteArrayOutputStream(1024);

                    membershipList.writeDelimitedTo(aOutput);

                    byte aSendData[] = aOutput.toByteArray();
                    InetAddress aIp = InetAddress.getByName(server.getHostName());// or aReceivePacket.getAddress();

                    DatagramPacket aSendPacket = new DatagramPacket(aSendData, aSendData.length, aIp, 9876);
                    aClientSocket.send(aSendPacket);

                } catch (IOException e) {
                    logger.logLine(Logger.WARNING, "Client socket failed while connecting to " + server + e);
                }
            }
        }
    }
}