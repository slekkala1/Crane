package tempest.services;

import tempest.protos.Membership;
import tempest.Machines;
import tempest.interfaces.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.*;
import java.util.Arrays;

public class GossipServer {

    private final Logger logger;
    private final int port;
    private ServiceRunner runner;
    private GossipClient gossipClient;

    private Membership.MembershipList membershipList = MembershipListUtil.getNewMembershipList();

    public GossipServer(Logger logger, int port) {
        this.logger = logger;
        this.port = port;
    }

    public void start() {
        if (runner != null)
            return;
        runner = new ServiceRunner();
        new Thread(runner).start();
        if (gossipClient !=null)
            return;
        try {
            gossipClient = new GossipClient(membershipList, new Machines().getMachines(), logger, 2);//do I need to know which machines are up before sending gossip?
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        new Thread(gossipClient).start();
        gossipClient = null;
    }

    public void stop() {
        if (runner == null)
            return;
        runner.stop();
        runner = null;
        if (gossipClient == null)
            return;
        gossipClient.stop();
        gossipClient = null;
    }

    class ServiceRunner implements Runnable {
        private boolean isRunning = true;
        private DatagramSocket serverSocket;
        private volatile boolean stopped = false;

        public void run() {
            try {
                serverSocket = new DatagramSocket(port);
                serverSocket.setSoTimeout(1000);   // set the timeout in millisecounds.
                logger.logLine(Logger.INFO, "Started Daemon server on" + InetAddress.getLocalHost().getHostName());
                byte[] receiveData = new byte[1024];

                while (isRunning) {
                    Arrays.fill(receiveData, (byte) 0);
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    try {
                        serverSocket.receive(receivePacket);
                    } catch (SocketTimeoutException e) {
                        continue;
                    }
                    ByteArrayInputStream inputStream = new ByteArrayInputStream(receiveData);
                    Membership.MembershipList receivedMembershipList = Membership.MembershipList.parseDelimitedFrom(inputStream);
                    //logger.logLine(Logger.INFO, "Current Membership list " + membershipList.toString());
                    //logger.logLine(Logger.INFO, "Recieved Membership list " + receivedMembershipList.toString());

                    membershipList = MembershipListUtil.mergeMembershipList(receivedMembershipList, membershipList);
                    //logger.logLine(Logger.INFO, "Merged Membership list " + membershipList.toString());
                }
                stopped = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void stop() {
            isRunning = false;
            if (serverSocket == null)
                return;
            while (!stopped) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            serverSocket.close();
            stopped = false;
        }
    }
}