package tempest;

import asg.cliche.ShellFactory;
import tempest.commands.handler.GrepHandler;
import tempest.commands.handler.IntroduceHandler;
import tempest.commands.handler.LeaveHandler;
import tempest.commands.handler.PingHandler;
import tempest.commands.interfaces.CommandHandler;
import tempest.commands.interfaces.ResponseCommandExecutor;
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
    private final ResponseCommandExecutor[] commandHandlers;

    public TempestApp() throws IOException {
        String logFile = "machine." + Inet4Address.getLocalHost().getHostName() + ".log";
        Logger logger = new DefaultLogger(new CommandLineExecutor(), new DefaultLogWrapper(), logFile, logFile);
        membershipService = new MembershipService(logger);
        commandHandlers = new ResponseCommandExecutor[] { new PingHandler(), new GrepHandler(logger), new IntroduceHandler(membershipService, logger), new LeaveHandler(membershipService)};
        Client client = new Client(membershipService, logger, commandHandlers);
        server = new Server(logger, 4444, commandHandlers);
        gossipServer = new GossipServer(membershipService, logger, 9876);
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
