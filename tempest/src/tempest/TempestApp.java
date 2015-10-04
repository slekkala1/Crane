package tempest;

import asg.cliche.ShellFactory;
import tempest.commands.handler.GrepHandler;
import tempest.commands.handler.PingHandler;
import tempest.interfaces.CommandHandler;
import tempest.interfaces.Logger;
import tempest.services.*;

import java.io.IOException;

public class TempestApp implements Runnable {
    private final Console console;
    private final Server server;
    private final Machines machines;
    private final GossipServer gossipServer;
    private final CommandHandler[] commandHandlers;

    public TempestApp() throws IOException {
        machines = new Machines();
        String logFile = "machine." + machines.getMachineNumber() + ".log";
        String grepFile = "vm" + (machines.getMachineNumber() + 1) + ".log";
        Logger logger = new DefaultLogger(new CommandLineExecutor(), new DefaultLogWrapper(), logFile, grepFile);
        commandHandlers = new CommandHandler[] { new PingHandler(), new GrepHandler(logger)};
        Client client = new Client(machines, logger, commandHandlers);
        server = new Server(logger, 4444, commandHandlers);
        gossipServer = new GossipServer(logger, 9876);
        console = new Console(logger, client, server, gossipServer);
    }

    public void run() {
        try {
            server.start();
            ShellFactory.createConsoleShell("Tempest", "", console).commandLoop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        new TempestApp().run();
    }
}
