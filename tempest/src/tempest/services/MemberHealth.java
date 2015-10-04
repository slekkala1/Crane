package tempest.services;

public class MemberHealth {

    private long timestamp;
    private int heartbeat;
    private NodeStatus nodeStatus;

    public MemberHealth(long timestamp, int heartbeat, NodeStatus nodeStatus) {
        this.heartbeat = heartbeat;
        this.timestamp = timestamp;
        this.nodeStatus = nodeStatus;
    }

    public int getHeartbeat() {
        return heartbeat;
    }

    public void setHeartbeat(int heartbeat) {
        this.heartbeat = heartbeat;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }


    public NodeStatus getNodeStatus() {
        return nodeStatus;
    }

    public void setNodeStatus(NodeStatus nodeStatus) {
        this.nodeStatus = nodeStatus;
    }
}
