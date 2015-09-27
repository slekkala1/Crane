package tempest.services;

import tempest.commands.server.Grep;
import tempest.commands.server.Ping;
import tempest.interfaces.Logger;
import tempest.interfaces.ServerCommand;
import tempest.networking.TcpServiceRunner;
import tempest.networking.UdpServiceRunner;

public class Server {
    private final Logger logger;
    private final int port;
    private TcpServiceRunner tcpRunner;
    private UdpServiceRunner udpRunner;

    public Server(Logger logger, int port) {
        this.logger = logger;
        this.port = port;
    }

    public void start() {
        if (tcpRunner == null) {
            tcpRunner = new TcpServiceRunner(logger, port, new ServerCommand[] { new Ping(), new Grep(logger)});
            new Thread(tcpRunner).start();
        }

        if (udpRunner == null) {
            udpRunner = new UdpServiceRunner(logger, port, new ServerCommand[]{new Ping(), new Grep(logger)});
            new Thread(udpRunner).start();
        }
    }

    public void stop() {
        if (tcpRunner != null) {
            tcpRunner.stop();
            tcpRunner = null;
        }
        if (udpRunner != null) {
            udpRunner.stop();
            udpRunner = null;
        }
    }


}
