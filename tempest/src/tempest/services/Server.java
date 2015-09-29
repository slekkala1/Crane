package tempest.services;

import tempest.interfaces.CommandHandler;
import tempest.interfaces.Logger;
import tempest.networking.TcpServiceRunner;
import tempest.networking.UdpServiceRunner;

public class Server {
    private final Logger logger;
    private final int port;
    private final CommandHandler[] commandHandlers;
    private TcpServiceRunner tcpRunner;
    private UdpServiceRunner udpRunner;

    public Server(Logger logger, int port, CommandHandler[] commandHandlers) {
        this.logger = logger;
        this.port = port;
        this.commandHandlers = commandHandlers;
    }

    public void start() {
        if (tcpRunner == null) {
            tcpRunner = new TcpServiceRunner(logger, port, commandHandlers);
            new Thread(tcpRunner).start();
        }

        if (udpRunner == null) {
            udpRunner = new UdpServiceRunner(logger, port, commandHandlers);
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
