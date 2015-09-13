package tempest;

import asg.cliche.ShellFactory;
import tempest.services.*;

import java.io.IOException;

public class TempestApp implements Runnable {
    private final Console console;
    private final Server server;
    private final Machines machines;

    public TempestApp() throws IOException {
        machines = new Machines();
        Logger logger = new Logger(machines, new CommandLineExecutor(), new DefaultLogWrapper());
        Client client = new Client(machines, logger);
        server = new Server(logger);
        console = new Console(logger, client, server);
    }

    @Override
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
