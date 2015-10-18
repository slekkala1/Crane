package tempest.services;

import tempest.commands.interfaces.CommandHandler;
import tempest.commands.interfaces.ResponseCommandExecutor;
import tempest.interfaces.Logger;
import tempest.networking.TcpServiceRunner;
import tempest.networking.UdpServiceRunner;

public class Server {
    private final Logger logger;
    private final int port;
    private final ResponseCommandExecutor[] commandHandlers;
    private TcpServiceRunner tcpRunner;
    private UdpServiceRunner udpRunner;

    public Server(Logger logger, int port, ResponseCommandExecutor[] commandHandlers) {
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
