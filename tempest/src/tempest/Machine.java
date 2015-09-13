package tempest;

public class Machine {
    private final String hostName;
    private final int port;

    public Machine(String hostName, int port) {
        this.hostName = hostName;
        this.port = port;
    }

    public String getHostName() {
        return hostName;
    }

    public int getPort() {
        return port;
    }
}
