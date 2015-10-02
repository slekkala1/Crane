package tempest.services;

import tempest.Machine;
import tempest.Machines;
import tempest.commands.client.Grep;
import tempest.commands.client.Ping;
import tempest.commands.response.Response;
import tempest.interfaces.ClientCommand;
import tempest.interfaces.CommandResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
        return new ClientCommandExecutor<Response>(machine, new Grep(options)).execute();
    }

    public Response grepAll(String options) {
        return executeAllParallel(new Grep(options));
    }

    public Response ping(Machine machine) {
        return new ClientCommandExecutor<Response>(machine, new Ping()).execute();
    }

    public Response pingAll() {
        return executeAllParallel(new Ping());
    }

    private <TResponse extends CommandResponse<TResponse>> TResponse executeAllParallel(ClientCommand<TResponse> command) {
        Collection<Callable<TResponse>> commandExecutors = new ArrayList<>();
        for (Machine machine : this.machines) {
            commandExecutors.add(new ClientCommandExecutor<TResponse>(machine, command));
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
                    logger.logLine(Logger.SEVERE, String.valueOf(e));
                }
            }
            return response;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    class ClientCommandExecutor<TResponse extends CommandResponse<TResponse>> implements Callable<TResponse> {
        private final Machine server;
        private final ClientCommand command;

        public ClientCommandExecutor(Machine server, ClientCommand command) {

            this.server = server;
            this.command = command;
        }

        public TResponse call() {
            return execute();
        }

        public TResponse execute() {
            String line;
            long startTime = System.currentTimeMillis();
            try {
                logger.logLine(Logger.INFO, "Connecting to " + server.getHostName() + " on port " + server.getPort());
                Socket socket = new Socket(server.getHostName(), server.getPort());
                logger.logLine(Logger.INFO, "Just connected to " + server.getHostName() + " on port " + server.getPort());
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                out.println(command.getRequest());
                socket.shutdownOutput();

                int lineCount = 0;
                StringBuilder builder = new StringBuilder();
                while ((line = in.readLine()) != null) {
                    builder.append(line).append(System.lineSeparator());
                    ++lineCount;
                }
                socket.close();
                long stopTime = System.currentTimeMillis();
                long elapsedTime = stopTime - startTime;
                return (TResponse) command.getResponse(builder.toString(), lineCount, elapsedTime);
            } catch (IOException e) {
                logger.logLine(Logger.WARNING, "Client socket failed while connecting to " + server + e);
                return null;
            }
        }
    }
}
