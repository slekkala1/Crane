package tempest.services;

import asg.cliche.Command;
import asg.cliche.Param;
import tempest.MembershipService;
import tempest.commands.Response;
import tempest.commands.ResponseData;
import tempest.interfaces.Logger;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;

public class Console {
    private final Logger logger;
    private final Client client;
    private final GossipClient gossipClient;
    private final Server server;
    private final GossipServer gossipServer;
    private final MembershipService membershipService;

    public Console(Logger logger, Client client, GossipClient gossipClient, Server server, GossipServer gossipServer, MembershipService membershipService) {
        this.logger = logger;
        this.client = client;
        this.gossipClient = gossipClient;
        this.server = server;
        this.gossipServer = gossipServer;
        this.membershipService = membershipService;
    }

    @Command(abbrev="sm")
    public void startMembership() {
        membershipService.start(client, gossipClient);
        gossipServer.start();
    }

    @Command(abbrev="stme")
    public void stopMembership() {
        membershipService.stop();
        gossipServer.stop();
    }

    @Command
    public void getMembershipList() {
        logger.logLine(membershipService.getMembershipList().toString(),logger.INFO);
    }

    @Command
    public void getSelfId() {
        //todo: return self ID as in demo instructions
    }

    @Command(abbrev="ss")
    public void serviceStart() {
        server.start();
    }

    @Command(abbrev="sest")
    public void serviceStop() {
        server.stop();
    }

    @Command
    public void log(@Param(name = "level") String level, @Param(name = "message") String message) {
        logger.logLine(level, message);
    }

    @Command
    public String grepLocal(@Param(name = "options") String options) throws InterruptedException, IOException {
        return logger.grep(options);
    }

    @Command
    public String grepAll(@Param(name = "options") String options) throws IOException, InterruptedException {
        Response<String> response = client.grepAll(options);
        return response.getResponse() + formatResponseStatistics(response.getResponseData());
    }

    @Command
    public String grepMachine(@Param(name = "machine", description = "host:port") String machine, @Param(name = "options") String options) throws IOException, InterruptedException {
        Response<String> response = client.grep(membershipService.getMember(machine.split(":")[0], Integer.parseInt(machine.split(":")[1])), options);
        return response.getResponse() + formatResponseStatistics(response.getResponseData());
    }

    @Command
    public String grepLocalToFile(@Param(name = "file") String file, @Param(name = "options") String options) throws InterruptedException, IOException {
        Files.write(FileSystems.getDefault().getPath(file), logger.grep(options).getBytes());
        return "Wrote to " + file;
    }

    @Command
    public String grepAllToFile(@Param(name = "file") String file, @Param(name = "options") String options) throws IOException, InterruptedException {
        Response<String> response = client.grepAll(options);
        Files.write(FileSystems.getDefault().getPath(file), response.getResponse().getBytes());
        return "Wrote to " + file + System.getProperty("line.separator") + formatResponseStatistics(response.getResponseData());
    }

    @Command
    public String grepMachineToFile(@Param(name = "file", description = "host:port") String file, @Param(name = "machine") String machine, @Param(name = "options") String options) throws IOException, InterruptedException {
        Response<String> response = client.grep(membershipService.getMember(machine.split(":")[0], Integer.parseInt(machine.split(":")[1])), options);
        Files.write(FileSystems.getDefault().getPath(file), response.getResponse().getBytes());
        return "Wrote to " + file + System.getProperty("line.separator") + formatResponseStatistics(response.getResponseData());
    }

    @Command
    public String pingAll() throws IOException, InterruptedException {
        Response<String> response = client.pingAll();
        return response.getResponse() + formatResponseStatistics(response.getResponseData());
    }

    @Command
    public String pingMachine(@Param(name = "machine", description = "host:port") String machine) throws IOException, InterruptedException {
        Response<String> response = client.ping(membershipService.getMember(machine.split(":")[0], Integer.parseInt(machine.split(":")[1])));
        return response.getResponse() + formatResponseStatistics(response.getResponseData());
    }

    private String formatResponseStatistics(ResponseData response) {
        StringBuilder resultBuilder = new StringBuilder("------------------------------");
        resultBuilder.append(System.getProperty("line.separator"));
        resultBuilder.append("Latency: ").append(response.getQueryLatency()).append("ms");
        resultBuilder.append(System.getProperty("line.separator"));
        return resultBuilder.toString();
    }
}