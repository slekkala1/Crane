package tempest.services;

import tempest.protos.Membership;
import tempest.Machines;
import tempest.interfaces.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.*;

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
                logger.logLine(Logger.INFO, "Started GossipServer server on" + InetAddress.getLocalHost().getHostName());

                while (isRunning) {
                    byte[] receiveData = new byte[1024];
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    serverSocket.receive(receivePacket);
                    ByteArrayInputStream inputStream = new ByteArrayInputStream(receiveData);
                    if(inputStream!=null) {
                        Membership.MembershipList receivedMembershipList = Membership.MembershipList.parseDelimitedFrom(inputStream);
                        logger.logLine(Logger.INFO, "Current Membership list " + membershipList.toString());

                        logger.logLine(Logger.INFO, "Recieved Membership list " + receivedMembershipList.toString());

                        membershipList = MembershipListUtil.mergeMembershipList(receivedMembershipList, membershipList);
                        logger.logLine(Logger.INFO, "Merged Membership list " + membershipList.toString());
                    }
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


}