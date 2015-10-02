package tempest;

import asg.cliche.ShellFactory;
import tempest.services.*;

import java.io.IOException;

public class TempestApp implements Runnable {
    private final Console console;
    private final Server server;
    private final Machines machines;
    private final Daemon daemon;

    public TempestApp() throws IOException {
        machines = new Machines();
        String logFile = "machine." + machines.getMachineNumber() + ".log";
        //String grepFile = "vm" + (machines.getMachineNumber() + 1) + ".log";
        Logger logger = new Logger(new CommandLineExecutor(), new DefaultLogWrapper(), logFile, logFile);
        Client client = new Client(machines, logger);
        server = new Server(logger, 4444);
        daemon = new Daemon(logger, 9876);
        console = new Console(logger, client, server);
    }

    @Override
    public void run() {
        try {
            server.start();
            daemon.start();
            ShellFactory.createConsoleShell("Tempest", "", console).commandLoop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        new TempestApp().run();
    }
}
