package tempest.services;

/**
 * Created by swapnalekkala on 9/27/15.
 */
public class MemberHealth {

    private long timestamp;
    private int heartbeat;

    public MemberHealth(long timestamp, int heartbeat) {
        this.heartbeat = heartbeat;
        this.timestamp = timestamp;
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
}
