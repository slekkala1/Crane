package tempest.services;

import asg.cliche.Command;
import java.io.IOException;

public class Console {
    private final Logger logger;
    private final Client client;
    private final Server server;

    public Console(Logger logger, Client client, Server server) {
        this.logger = logger;
        this.client = client;
        this.server = server;
    }

    @Command
    public void serviceStart() {
        server.start();
    }

    @Command
    public void serviceStop() {
        server.stop();
    }

    @Command
    public void log(String level, String message) {
        logger.logLine(level, message);
    }

    @Command
    public String grepLocal(String options) throws InterruptedException, IOException {
        return logger.grep(options);
    }

    @Command
    public String grepAll(String options) throws IOException, InterruptedException {
        return client.grepAll(options);
    }

    @Command
    public String grepMachine(String machine, String options) throws IOException, InterruptedException {
        return client.grep(machine, options);
    }

    @Command
    public String pingAll() throws IOException, InterruptedException {
        return client.pingAll();
    }

    @Command
    public String pingMachine(String machine) throws IOException, InterruptedException {
        return client.ping(machine);
    }
}
