package tempest;

import tempest.commands.Response;
import tempest.protos.Membership;
import tempest.services.Client;
import tempest.services.GossipClient;
import tempest.services.Heartbeat;
import tempest.services.MembershipListUtil;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Properties;

public class MembershipService {
    private final String introducer;
    private final Membership.Member localMachine;
    private Heartbeat heartbeat;
    private Client client;
    private Membership.MembershipList membershipList;

    public MembershipService() throws UnknownHostException {
        this(readPropertiesFile(), 4444);
    }

    public MembershipService(String introducer, int localPort) throws UnknownHostException {
        this.introducer = introducer;
        localMachine = Membership.Member.newBuilder().setHost(Inet4Address.getLocalHost().getHostName())
                .setPort(localPort)
                .setTimestamp(new Date().getTime())
                .build();
    }

    public void start(Client client, GossipClient gossipClient) {
        this.client = client;
        this.heartbeat = new Heartbeat(gossipClient);
        Membership.Member introduceMember = Membership.Member.newBuilder()
                .setHost(introducer.split(":")[0])
                .setPort(Integer.parseInt(introducer.split(":")[1]))
                .build();
        Response<Membership.MembershipList> introduceResponse = client.introduce(introduceMember, localMachine);
        membershipList = introduceResponse.getResponse();
        heartbeat.start();
    }

    public void stop() {
        heartbeat.stop();
        client.leave(localMachine);
    }

    public synchronized void addMember(Membership.Member member) {
        membershipList = MembershipListUtil.addMemberToMembershipList(member,membershipList);
    }

    public Membership.MembershipList getMembershipList() {
        return membershipList;
    }

    public Membership.Member getRandomMachine() {
        Membership.MembershipList snapshot = membershipList;
        int index = (int)(Math.random()* snapshot.getMemberCount());
        int localIndex = getLocalIndex(snapshot);
        while (index == localIndex) {
            index = (int)(Math.random()* snapshot.getMemberCount());
        }
        return snapshot.getMember(index);
    }

    public Membership.Member getMember(String host, int port) {
        Membership.MembershipList snapshot = membershipList;
        for (Membership.Member member : snapshot.getMemberList()) {
            if (member.getHost().equals(host) && member.getPort() == port)
                return member;
        }
        return null;
    }

    public Membership.Member getLocalMachine() {
        return localMachine;
    }

    public synchronized void update() {
        membershipList = MembershipListUtil.updateMembershipList(membershipList);
    }

    private int getLocalIndex(Membership.MembershipList snapshot) {
        int i = 0;
        for (Membership.Member member : snapshot.getMemberList()) {
            if (member.getHost().equals(localMachine.getHost())
                    && member.getPort() == localMachine.getPort()
                    && member.getTimestamp() == localMachine.getTimestamp())
                return i;
            ++i;
        }
        return -1;
    }

    private static String readPropertiesFile() {
        Properties prop = new Properties();

        try (InputStream inputStream = MembershipService.class.getClassLoader().getResourceAsStream("config.properties")) {

            if (inputStream != null) {
                prop.load(inputStream);
            } else {
                throw new FileNotFoundException("property file 'config.properties' not found in the classpath");
            }
        } catch (Exception e) {
            return "";
        }
        return prop.getProperty("introducer");
    }
}
