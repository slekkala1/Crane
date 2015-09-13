package tempest.services;

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
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

public class Client {
    private static ExecutorService pool = Executors.newFixedThreadPool(7);
    private final String[] machines;
    private final Logger logger;

    public Client(Machines machines, Logger logger) {
        this.machines = machines.getMachines();
        this.logger = logger;
    }

    public Response grep(String machine, String options) {
        return new ClientCommandExecutor<Response>(machine, new Grep(options)).execute();
    }

    public Response grepAll(String options) {
        return executeAllParallel(new Grep(options));
    }

    public Response ping(String machine) {
        return new ClientCommandExecutor<Response>(machine, new Ping()).execute();
    }

    public Response pingAll() {
        return executeAllParallel(new Ping());
    }

    private <TResponse extends CommandResponse<TResponse>> TResponse executeAllParallel(ClientCommand<TResponse> command) {
        long startTime = System.currentTimeMillis();
        Set<Future<TResponse>> futureSet = new HashSet<>();

        for (String machine : this.machines) {
            Callable<TResponse> callable = new ClientCommandExecutor<>(machine, command);
            Future<TResponse> future = pool.submit(callable);
            futureSet.add(future);
        }
        TResponse response = null;
        for (Future<TResponse> future : futureSet) {
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
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        logger.logLine(Logger.INFO, "Query latency" + elapsedTime);
        return response;
    }

    class ClientCommandExecutor<TResponse extends CommandResponse<TResponse>> implements Callable<TResponse> {
        private final String server;
        private final ClientCommand command;

        public ClientCommandExecutor(String server, ClientCommand command) {

            this.server = server;
            this.command = command;
        }

        public TResponse call() {
            return execute();
        }

        public TResponse execute() {
            String line;
            try {
                Socket socket = new Socket(server, 4444);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                out.println(command.getRequest());
                socket.shutdownOutput();

                int lineCount = 0;
                StringBuilder builder = new StringBuilder();
                while ((line = in.readLine()) != null) {
                    builder.append(line);
                    ++lineCount;
                }
                socket.close();
                return (TResponse) command.getResponse(builder.toString(), lineCount);
            } catch (IOException e) {
                logger.logLine(Logger.WARNING, "Client socket failed " + e);
                return null;
            }
        }
    }
}
