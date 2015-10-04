package tempest;

import asg.cliche.ShellFactory;
import tempest.commands.handler.GrepHandler;
import tempest.commands.handler.PingHandler;
import tempest.interfaces.CommandHandler;
import tempest.interfaces.Logger;
import tempest.services.*;

import java.io.IOException;
import java.net.Inet4Address;

public class TempestApp implements Runnable {
    private final Console console;
    private final Server server;
    private final MembershipService membershipService;
    private final GossipServer gossipServer;
    private final GossipClient gossipClient;
    private final CommandHandler[] commandHandlers;

    public TempestApp() throws IOException {
        membershipService = new MembershipService();
        String logFile = "machine." + Inet4Address.getLocalHost().getHostName() + ".log";
        Logger logger = new DefaultLogger(new CommandLineExecutor(), new DefaultLogWrapper(), logFile, logFile);
        commandHandlers = new CommandHandler[] { new PingHandler(), new GrepHandler(logger)};
        Client client = new Client(membershipService, logger, commandHandlers);
        server = new Server(logger, 4444, commandHandlers);
        gossipServer = new GossipServer(logger, 9876);
        gossipClient = new GossipClient(membershipService, logger);
        console = new Console(logger, client, gossipClient, server, gossipServer, membershipService);
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
