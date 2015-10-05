package tempest;

import tempest.commands.Response;
import tempest.interfaces.Logger;
import tempest.protos.Membership;
import tempest.services.*;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Properties;

public class MembershipService {
    private final Logger logger;
    private final String introducer;
    private Membership.Member localMember;
    private Heartbeat heartbeat;
    private Client client;
    private Membership.MembershipList membershipList;

    public MembershipService(Logger logger) throws UnknownHostException {
        this(logger, readPropertiesFile(), 4444);
    }

    public MembershipService(Logger logger, String introducer, int localPort) throws UnknownHostException {
        this.logger = logger;
        this.introducer = introducer;
        localMember = Membership.Member.newBuilder().setHost(Inet4Address.getLocalHost().getHostName())
                .setPort(localPort)
                .setTimestamp(new Date().getTime())
                .setNodeStatus(NodeStatus.ACTIVE.name())
                .build();
    }

    public void start(Client client, GossipClient gossipClient) {
        this.client = client;
        this.heartbeat = new Heartbeat(gossipClient);
        Membership.Member introduceMember = Membership.Member.newBuilder()
                .setHost(introducer.split(":")[0])
                .setPort(Integer.parseInt(introducer.split(":")[1]))
                .build();
        Response<Membership.MembershipList> introduceResponse = client.introduce(introduceMember, localMember);
        membershipList = introduceResponse.getResponse();
        heartbeat.start();
    }

    public void stop() {
        heartbeat.stop();
        client.leave(localMember);
    }

    public synchronized void addMember(Membership.Member member) {
        if(membershipList != null) {
            membershipList = membershipList.toBuilder().addMember(member).build();
        } else {
            membershipList = Membership.MembershipList.newBuilder().addMember(member).build();
        }
    }

    public synchronized void memberLeft(Membership.Member memberLeft) {
        int removeIndex = -1;
        for (int i = 0; i < membershipList.getMemberList().size(); i++) {
            if (membershipList.getMemberList().get(i).getHost().equals(memberLeft.getHost())) {
                removeIndex = i;
                break;
            }
        }
        if (removeIndex >= 0) {
            Membership.Member updatedMember = memberLeft.toBuilder().setNodeStatus(NodeStatus.LEAVE.name()).build();
            membershipList = membershipList.toBuilder().removeMember(removeIndex)
                                .addMember(updatedMember).build();
        }
    }

    public void merge(Membership.MembershipList otherList) {
        membershipList = MembershipListUtil.mergeMembershipList(otherList, membershipList);
    }

    public Membership.MembershipList getMembershipList() {
        return membershipList;
    }

    public Membership.Member getRandomMachine() {
        Membership.MembershipList snapshot = membershipList;
        int index = (int) (Math.random() * snapshot.getMemberCount());
        int localIndex = getLocalIndex(snapshot);
        while (index == localIndex) {
            index = (int) (Math.random() * snapshot.getMemberCount());
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

    public synchronized void update() {
        Membership.MembershipList.Builder membershipListBuilder = membershipList.toBuilder();
        for (Membership.Member.Builder memberBuilder : membershipListBuilder.getMemberBuilderList()) {
            if (memberBuilder.getHost().equals(localMember.getHost())
                    && memberBuilder.getPort() == localMember.getPort()
                    && memberBuilder.getTimestamp() == localMember.getTimestamp()) {
                memberBuilder.setHearbeat(memberBuilder.getHearbeat() + 1);
            }
        }
        membershipList = membershipListBuilder.build();
    }

    private int getLocalIndex(Membership.MembershipList snapshot) {
        int i = 0;
        for (Membership.Member member : snapshot.getMemberList()) {
            if (member.getHost().equals(localMember.getHost())
                    && member.getPort() == localMember.getPort()
                    && member.getTimestamp() == localMember.getTimestamp())
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
