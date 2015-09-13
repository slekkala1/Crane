package tempest.services;

import asg.cliche.Command;
import asg.cliche.Param;
import tempest.commands.response.Response;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;

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
    public void log(@Param(name="level")String level, @Param(name="message")String message) {
        logger.logLine(level, message);
    }

    @Command
    public String grepLocal(@Param(name="options")String options) throws InterruptedException, IOException {
        return logger.grep(options);
    }

    @Command
    public String grepAll(@Param(name="options")String options) throws IOException, InterruptedException {
        return client.grepAll(options).getResponse();
    }

    @Command
    public String grepMachine(@Param(name="machine")String machine, @Param(name="options")String options) throws IOException, InterruptedException {
        return client.grep(machine, options).getResponse();
    }

    @Command
    public String grepLocalToFile(@Param(name="file")String file, @Param(name="options")String options) throws InterruptedException, IOException {
        Files.write(FileSystems.getDefault().getPath(file), logger.grep(options).getBytes());
        return "Wrote to " + file;
    }

    @Command
    public String grepAllToFile(@Param(name="file")String file, @Param(name="options")String options) throws IOException, InterruptedException {
        Response response = client.grepAll(options);
        Files.write(FileSystems.getDefault().getPath(file), response.getResponse().getBytes());
        return "Wrote " + response.getLineCount() + " lines to " + file;
    }

    @Command
    public String grepMachineToFile(@Param(name="file")String file, @Param(name="machine")String machine, @Param(name="options")String options) throws IOException, InterruptedException {
        Response response = client.grep(machine, options);
        Files.write(FileSystems.getDefault().getPath(file), response.getResponse().getBytes());
        return "Wrote " + response.getLineCount() + " lines to " + file;
    }

    @Command
    public String pingAll() throws IOException, InterruptedException {
        return client.pingAll().getResponse();
    }

    @Command
    public String pingMachine(@Param(name="machine")String machine) throws IOException, InterruptedException {
        return client.ping(machine).getResponse();
    }
}