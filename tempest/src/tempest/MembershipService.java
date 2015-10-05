package tempest;

import tempest.commands.Response;
import tempest.interfaces.Logger;
import tempest.protos.Membership;
import tempest.services.*;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class MembershipService {
    private final Logger logger;
    private final String introducer;
    private final int localPort;
    private MemberHealth localMemberHealth;
    private Heartbeat heartbeat;
    private Client client;
    private final List<MemberHealth> memberHealths = new ArrayList<>();

    public MembershipService(Logger logger) throws UnknownHostException {
        this(logger, readPropertiesFile(), 4444);
    }

    public MembershipService(Logger logger, String introducer, int localPort) throws UnknownHostException {
        this.logger = logger;
        this.introducer = introducer;
        this.localPort = localPort;
        localMemberHealth = new MemberHealth(Inet4Address.getLocalHost().getHostName(), localPort, System.currentTimeMillis(), 0);
    }

    public void start(Client client, GossipClient gossipClient) throws UnknownHostException {
        this.client = client;
        this.heartbeat = new Heartbeat(gossipClient);
        Membership.Member introduceMember = Membership.Member.newBuilder()
                .setHost(introducer.split(":")[0])
                .setPort(Integer.parseInt(introducer.split(":")[1]))
                .build();
        localMemberHealth = new MemberHealth(Inet4Address.getLocalHost().getHostName(), localPort, System.currentTimeMillis(), 0);
        Response<Membership.MembershipList> introduceResponse = client.introduce(introduceMember, localMemberHealth.toMember());
        merge(introduceResponse.getResponse());
        heartbeat.start();
    }

    public void stop() {
        heartbeat.stop();
        client.leave(localMemberHealth.toMember());
        memberHealths.clear();

    }

    public synchronized void addMember(Membership.Member member) {
        if (localMemberHealth.matches(member)) {
            return;
        }
        MemberHealth memberHealth = new MemberHealth(member);
        memberHealths.add(memberHealth);
        logger.logLine(Logger.INFO, "Member: " + memberHealth.getId() + " has joined");
    }

    public synchronized void memberLeft(Membership.Member memberLeft) {
        for (MemberHealth memberHealth : memberHealths) {
            if (memberHealth.matches(memberLeft)) {
                memberHealth.setHasLeft(true);
                logger.logLine(Logger.INFO, "Member: " + memberHealth.getId() + " has left");
            }
        }
    }

    public synchronized void merge(Membership.MembershipList membershipList) {
        for (Membership.Member member : membershipList.getMemberList()) {
            if (localMemberHealth.matches(member)) {
                continue;
            }
            boolean merged = false;
            for(MemberHealth memberHealth : memberHealths) {
                if (memberHealth.matches(member)) {
                    memberHealth.merge(member);
                    merged = true;
                    continue;
                }
            }
            if (!merged) {
                MemberHealth memberHealth = new MemberHealth(member);
                memberHealths.add(memberHealth);
                logger.logLine(Logger.INFO, "Member: " + memberHealth.getId() + " has joined");
            }
        }
    }
    public synchronized Membership.MembershipList getMembershipList() {
        Membership.MembershipList.Builder builder = Membership.MembershipList.newBuilder().addMember(localMemberHealth.toMember());
        for (MemberHealth memberHealth : memberHealths) {
            if (!memberHealth.hasLeft()) {
                builder.addMember(memberHealth.toMember());
            }
        }
        return builder.build();
    }

    public synchronized Membership.MembershipList getMembershipListNoLocal() {
        Membership.MembershipList.Builder builder = Membership.MembershipList.newBuilder();
        for (MemberHealth memberHealth : memberHealths) {
            if (!memberHealth.hasLeft() && !memberHealth.hasFailed()) {
                builder.addMember(memberHealth.toMember());
            }
        }
        return builder.build();
    }

    public synchronized Membership.Member getRandomMachine() {
        int index = (int) (Math.random() * memberHealths.size());
        return memberHealths.toArray(new MemberHealth[memberHealths.size()])[index].toMember();
    }

    public synchronized Membership.Member getMember(String host, int port) {
        for (MemberHealth memberHealth : memberHealths) {
            if (memberHealth.getHost().equals(host) && memberHealth.getPort() == port)
                return memberHealth.toMember();
        }
        return null;
    }

    public synchronized void update() {
        localMemberHealth.setHeartbeat(localMemberHealth.getHeartbeat() + 1);
        long currentTime = System.currentTimeMillis();
        List<MemberHealth> removals = new ArrayList<>();
        for (MemberHealth memberHealth : memberHealths) {
            if (currentTime - memberHealth.getLastSeen() > 5750) {
                removals.add(memberHealth);
            }
            else if (currentTime - memberHealth.getLastSeen() > 2750) {
                if (!memberHealth.hasFailed() && !memberHealth.hasLeft()) {
                    memberHealth.setHasFailed(true);
                    logger.logLine(Logger.INFO, "Member: " + memberHealth.getId() + " has failed");
                }
            }
            else {
                memberHealth.setHasFailed(false);
            }
        }
        for (MemberHealth memberHealth : removals) {
            memberHealths.remove(memberHealth);
            logger.logLine(Logger.INFO, "Member: " + memberHealth.getId() + " has been removed");
        }
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
