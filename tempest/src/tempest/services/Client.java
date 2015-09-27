package tempest.services;

import tempest.Machine;
import tempest.Machines;
import tempest.commands.client.Grep;
import tempest.commands.client.Ping;
import tempest.commands.response.Response;
import tempest.interfaces.*;
import tempest.networking.TcpClientCommandExecutor;
import tempest.networking.UdpClientCommandExecutor;

import java.util.*;
import java.util.concurrent.*;

public class Client {
    private static ExecutorService pool = Executors.newFixedThreadPool(7);
    private final Machine[] machines;
    private final Logger logger;

    public Client(Machines machines, Logger logger) {
        this.machines = machines.getMachines();
        this.logger = logger;
    }

    public Response grep(Machine machine, String options) {
        return createExecutor(machine, new Grep(options)).execute();
    }

    public Response grepAll(String options) {
        return executeAllParallel(new Grep(options));
    }

    public Response ping(Machine machine) {
        return createExecutor(machine, new Ping()).execute();
    }

    public Response pingAll() {
        return executeAllParallel(new Ping());
    }

    private <TResponse extends CommandResponse<TResponse>> TResponse executeAllParallel(ClientCommand<TResponse> command) {
        Collection<Callable<TResponse>> commandExecutors = new ArrayList<>();
        for (Machine machine : this.machines) {
            commandExecutors.add(createExecutor(machine, command));
        }
        List<Future<TResponse>> results;
        try {
            results = pool.invokeAll(commandExecutors);
            TResponse response = null;
            for (Future<TResponse> future : results) {
                try {
                    if (response == null)
                        response = future.get();
                    else {
                        TResponse tResponse = future.get();
                        if (tResponse != null) {
                            response = response.add(tResponse);
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

    private <TResponse extends CommandResponse<TResponse>> ClientCommandExecutor<TResponse> createExecutor(Machine machine, ClientCommand<TResponse> command) {
        if (command instanceof UdpClientCommand)
            return new UdpClientCommandExecutor<>(machine, (UdpClientCommand)command, logger);
        return new TcpClientCommandExecutor<>(machine, command, logger);
    }
}
