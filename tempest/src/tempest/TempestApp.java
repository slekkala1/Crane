package tempest;

import asg.cliche.ShellFactory;
import tempest.commands.command.Membership;
import tempest.commands.handler.*;
import tempest.commands.interfaces.CommandExecutor;
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
    private final CommandExecutor[] commandHandlers;
    private final ResponseCommandExecutor[] responseCommandHandlers;

    public TempestApp() throws IOException {
        String logFile = "machine." + Inet4Address.getLocalHost().getHostName() + ".log";
        Logger logger = new DefaultLogger(new CommandLineExecutor(), new DefaultLogWrapper(), logFile, logFile);
        membershipService = new MembershipService(logger);
        commandHandlers = new CommandExecutor[] { new MembershipHandler(membershipService)};
        responseCommandHandlers = new ResponseCommandExecutor[] { new PingHandler(), new GrepHandler(logger), new IntroduceHandler(membershipService, logger), new LeaveHandler(membershipService)};
        Client client = new Client(membershipService, logger, commandHandlers, responseCommandHandlers);
        server = new Server(logger, 4444, commandHandlers, responseCommandHandlers);
        console = new Console(logger, client, server, membershipService);
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
