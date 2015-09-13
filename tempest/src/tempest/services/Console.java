package tempest.services;

import asg.cliche.Command;
import asg.cliche.Param;
import tempest.xcommands.response.Response;
import tempest.interfaces.CommandResponse;

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
        Response response = client.grepAll(options);
        return response.getResponse() + formatResponseStatistics(response);
    }

    @Command
    public String grepMachine(@Param(name="machine")String machine, @Param(name="options")String options) throws IOException, InterruptedException {
        Response response = client.grep(machine, options);
        return response.getResponse() + formatResponseStatistics(response);
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
        return "Wrote to " + file + System.getProperty("line.separator") + formatResponseStatistics(response);
    }

    @Command
    public String grepMachineToFile(@Param(name="file")String file, @Param(name="machine")String machine, @Param(name="options")String options) throws IOException, InterruptedException {
        Response response = client.grep(machine, options);
        Files.write(FileSystems.getDefault().getPath(file), response.getResponse().getBytes());
        return "Wrote to " + file + System.getProperty("line.separator") + formatResponseStatistics(response);
    }

    @Command
    public String pingAll() throws IOException, InterruptedException {
        Response response = client.pingAll();
        return response.getResponse() + formatResponseStatistics(response);
    }

    @Command
    public String pingMachine(@Param(name="machine")String machine) throws IOException, InterruptedException {
        Response response = client.ping(machine);
        return response.getResponse() + formatResponseStatistics(response);
    }

    private String formatResponseStatistics(CommandResponse response) {
        StringBuilder resultBuilder = new StringBuilder("------------------------------");
        resultBuilder.append(System.getProperty("line.separator"));
        resultBuilder.append("Lines: ").append(response.getLineCount());
        resultBuilder.append(System.getProperty("line.separator"));
        resultBuilder.append("Latency: ").append(response.getQueryLatency()).append("ms");
        resultBuilder.append(System.getProperty("line.separator"));
        return resultBuilder.toString();
    }
}