package tempest.sdfs.client;

import tempest.commands.Response;
import tempest.commands.command.*;
import tempest.commands.handler.*;
import tempest.commands.interfaces.ResponseCommand;
import tempest.commands.interfaces.ResponseCommandExecutor;
import tempest.commands.interfaces.Udp;
import tempest.interfaces.ClientResponseCommandExecutor;
import tempest.interfaces.Logger;
import tempest.networking.TcpClientResponseCommandExecutor;
import tempest.networking.UdpClientResponseCommandExecutor;
import tempest.protos.Membership;
import tempest.services.*;

import java.io.IOException;
import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SDFSClient {

    private List<String> allMachines = new ArrayList<String>(){{
            add("fa15-cs425-g03-01.cs.illinois.edu:4444");
            add("fa15-cs425-g03-02.cs.illinois.edu:4444");
            add("fa15-cs425-g03-03.cs.illinois.edu:4444");
            add("fa15-cs425-g03-04.cs.illinois.edu:4444");
            add("fa15-cs425-g03-05.cs.illinois.edu:4444");
            add("fa15-cs425-g03-06.cs.illinois.edu:4444");
            add("fa15-cs425-g03-07.cs.illinois.edu:4444");
        }};


    private ResponseCommandExecutor[] responseCommandHandlers;
    private final Logger logger;

    public String getRandomMachine() {
        int idx = new Random().nextInt(allMachines.size());
        String randomMachine = allMachines.get(idx);
        return randomMachine;
    }


    public SDFSClient() throws IOException {
        String logFile = "machine." + Inet4Address.getLocalHost().getHostName() + ".log";
        logger = new DefaultLogger(new CommandLineExecutor(), new DefaultLogWrapper(), logFile, logFile);
        responseCommandHandlers = new ResponseCommandExecutor[] {new GetHandler(), new PutHandler(), new DeleteHandler()};
    }

    public Response delete(String sDFSFileName) {
        Delete delete = new Delete();
        delete.setRequest(sDFSFileName);
        String randomMachine = getRandomMachine();
        Membership.Member member = Membership.Member.newBuilder()
                .setHost(randomMachine.split(":")[0])
                .setPort(Integer.parseInt(randomMachine.split(":")[1]))
                .build();

        return createResponseExecutor(member, new Delete()).execute();
    }

    public Response get(String sDFSFileName) {
        Get get = new Get();
        get.setRequest(sDFSFileName);
        String randomMachine = getRandomMachine();
        Membership.Member member = Membership.Member.newBuilder()
                .setHost(randomMachine.split(":")[0])
                .setPort(Integer.parseInt(randomMachine.split(":")[1]))
                .build();
        return createResponseExecutor(member, get).execute();
    }

    public Response put(String localFileName, String sdfsFileName) {
        Put put = new Put();
        put.setRequest(sdfsFileName);
        put.setLocalFileName(localFileName);
        String randomMachine = getRandomMachine();

        Membership.Member member = Membership.Member.newBuilder()
                .setHost(randomMachine.split(":")[0])
                .setPort(Integer.parseInt(randomMachine.split(":")[1]))
                .build();
        return createResponseExecutor(member, put).execute();
    }

    private <TRequest, TResponse> ClientResponseCommandExecutor<TResponse> createResponseExecutor(Membership.Member member, ResponseCommand<TRequest, TResponse> command) {
        ResponseCommandExecutor commandHandler = null;
        for (ResponseCommandExecutor ch : responseCommandHandlers) {
            if (ch.canHandle(command.getType()))
                commandHandler = ch;
        }
        if (command instanceof Udp)
            return new UdpClientResponseCommandExecutor<>(member, command, commandHandler, logger);
        return new TcpClientResponseCommandExecutor<>(member, command, commandHandler, logger);
    }
}