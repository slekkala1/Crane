package tempest.services;

import tempest.MembershipService;
import tempest.commands.Response;
import tempest.commands.command.Grep;
import tempest.commands.command.Introduce;
import tempest.commands.command.Leave;
import tempest.commands.command.Ping;
import tempest.interfaces.*;
import tempest.networking.TcpClientCommandExecutor;
import tempest.networking.UdpClientCommandExecutor;
import tempest.protos.Membership;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

public class Client {
    private static ExecutorService pool = Executors.newFixedThreadPool(7);
    private final MembershipService membershipService;
    private final Logger logger;
    private final CommandHandler[] commandHandlers;

    public Client(MembershipService membershipService, Logger logger, CommandHandler[] commandHandlers) {
        this.membershipService = membershipService;
        this.logger = logger;
        this.commandHandlers = commandHandlers;
    }

    public Response grep(Membership.Member member, String options) {
        Grep grep = new Grep();
        grep.setRequest(options);
        return createExecutor(member, grep).execute();
    }

    public Response grepAll(String options) {
        Grep grep = new Grep();
        grep.setRequest(options);
        return executeAllParallel(grep);
    }

    public Response introduce(Membership.Member member, Membership.Member localMember) {
        Introduce introduce = new Introduce();
        introduce.setRequest(localMember);
        return createExecutor(member, introduce).execute();
    }

    public Response leave(Membership.Member localMember) {
        Leave introduce = new Leave();
        introduce.setRequest(localMember);
        return executeAllParallel(introduce);
    }

    public Response ping(Membership.Member member) {
        return createExecutor(member, new Ping()).execute();
    }

    public Response pingAll() {
        return executeAllParallel(new Ping());
    }

    private <TRequest, TResponse> Response<TResponse> executeAllParallel(Command<TRequest, TResponse> command) {
        Collection<Callable<Response<TResponse>>> commandExecutors = new ArrayList<>();
        for (Membership.Member machine : membershipService.getMembershipList().getMemberList()) {
            commandExecutors.add(createExecutor(machine, command));
        }
        List<Future<Response<TResponse>>> results;
        try {
            results = pool.invokeAll(commandExecutors);
            Response<TResponse> response = null;
            for (Future<Response<TResponse>> future : results) {
                try {
                    if (response == null)
                        response = future.get();
                    else {
                        Response<TResponse> tResponse = future.get();
                        if (tResponse != null) {
                            response.setResponseData(response.getResponseData().add(tResponse.getResponseData()));
                            response.setResponse(command.add(response.getResponse(), tResponse.getResponse()));
                        }
                    }
                } catch (ExecutionException e) {
                    logger.logLine(DefaultLogger.SEVERE, String.valueOf(e));
                }
            }
            return response;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    private <TRequest, TResponse> ClientCommandExecutor<TResponse> createExecutor(Membership.Member member, Command<TRequest, TResponse> command) {
        CommandHandler commandHandler = null;
        for (CommandHandler ch : commandHandlers) {
            if (ch.canHandle(command.getCommandId()))
                commandHandler = ch;
        }
        if (command instanceof UdpCommand)
            return new UdpClientCommandExecutor<>(member, command, commandHandler, logger);
        return new TcpClientCommandExecutor<>(member, command, commandHandler, logger);
    }
}
