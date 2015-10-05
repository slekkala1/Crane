package tempest.services;

import tempest.protos.Membership;

public class MemberHealth {
    private final String host;
    private final int port;
    private final long timestamp;
    private long lastSeen;
    private int heartbeat;
    private boolean hasLeft;
    private boolean hasFailed;

    public MemberHealth(Membership.Member member) {
        this(member.getHost(), member.getPort(), member.getTimestamp(), member.getHearbeat());
    }

    public MemberHealth(String host, int port, long timestamp, int heartbeat) {
        this.host = host;
        this.port = port;
        this.timestamp = timestamp;
        lastSeen = System.currentTimeMillis();
        this.heartbeat = heartbeat;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getLastSeen() {
        return lastSeen;
    }

    public boolean getHasLeft() {
        return hasLeft;
    }

    public void setHasLeft(boolean hasLeft) {
        this.hasLeft = hasLeft;
    }

    public boolean isHasFailed() {
        return hasFailed;
    }

    public void setHasFailed(boolean hasFailed) {
        this.hasFailed = hasFailed;
    }

    public int getHeartbeat() {
        return heartbeat;
    }

    public void setHeartbeat(int heartbeat) {
        this.heartbeat = heartbeat;
    }

    public boolean matches(Membership.Member member) {
        return host.equals(member.getHost())
                && port == member.getPort()
                && timestamp == member.getTimestamp();
    }

    public void merge(Membership.Member member) {
        if (heartbeat < member.getHearbeat()) {
            heartbeat = member.getHearbeat();
            lastSeen = System.currentTimeMillis();
        }
    }

    public Membership.Member toMember() {
        return Membership.Member.newBuilder()
                .setHost(host)
                .setPort(port)
                .setTimestamp(timestamp)
                .setHearbeat(heartbeat)
                .build();
    }

    public String getId() {
        return getHost() + ":" + getPort() + ":" + getTimestamp();
    }
}
