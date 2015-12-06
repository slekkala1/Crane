package tempest.services;

import tempest.commands.interfaces.CommandExecutor;
import tempest.commands.interfaces.ResponseCommandExecutor;
import tempest.interfaces.Logger;
import tempest.networking.TcpServiceRunner;
import tempest.networking.UdpServiceRunner;

public class Server {
    private final Logger logger;
    private final int port;
    private final CommandExecutor[] commandHandlers;
    private final ResponseCommandExecutor[] responseCommandHandlers;
    private TcpServiceRunner tcpRunner,tcpRunner1;
    private UdpServiceRunner udpRunner;

    public Server(Logger logger, int port, CommandExecutor[] commandHandlers, ResponseCommandExecutor[] responseCommandHandlers) {
        this.logger = logger;
        this.port = port;
        this.commandHandlers = commandHandlers;
        this.responseCommandHandlers = responseCommandHandlers;
    }

    public void start() {
        if (tcpRunner == null) {
            tcpRunner = new TcpServiceRunner(logger, port, responseCommandHandlers);
            new Thread(tcpRunner).start();
            //need to remove below server
            tcpRunner1 = new TcpServiceRunner(logger, 4445, responseCommandHandlers);
            new Thread(tcpRunner1).start();
        }

        if (udpRunner == null) {
            udpRunner = new UdpServiceRunner(logger, port, commandHandlers, responseCommandHandlers);
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
