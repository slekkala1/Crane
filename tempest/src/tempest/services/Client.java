package tempest.services;

import tempest.Machines;
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

    public String grep(String machine, String options) {
        return new ClientCommandExecutor(machine, "grep " + options).execute();
    }

    public String grepAll(String options) {
        return executeAllParallel("grep " + options);
    }

    public String ping(String machine) {
        return new ClientCommandExecutor(machine, "ping").execute();
    }

    public String pingAll() {
        return executeAllParallel("ping");
    }

    private String executeAllParallel(String command) {
        long startTime = System.currentTimeMillis();
        Set<Future<String>> futureSet = new HashSet<>();

        for (String machine : this.machines) {
            Callable<String> callable = new ClientCommandExecutor(machine, command);
            Future<String> future = pool.submit(callable);
            futureSet.add(future);
        }
        StringBuilder resultBuilder = new StringBuilder();
        for (Future<String> future : futureSet) {
            try {
                resultBuilder.append(future.get());
            } catch (ExecutionException e) {
                logger.logLine(Logger.SEVERE, String.valueOf(e));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        logger.logLine(Logger.INFO, "Query latency" + elapsedTime);
        return resultBuilder.toString();
    }

    class ClientCommandExecutor implements Callable<String> {
        private final String server;
        private final String command;

        public ClientCommandExecutor(String server, String command) {

            this.server = server;
            this.command = command;
        }

        @Override
        public String call() {
            return execute();
        }

        public String execute(){
            String line;
            try{
                Socket socket = new Socket(server, 4444);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                out.println(command);
                socket.shutdownOutput();

                StringBuilder builder = new StringBuilder();
                while ((line = in.readLine()) != null) {
                    builder.append(line);
                }
                socket.close();
                return builder.toString();
            } catch (IOException e){
                System.out.println("Read failed");
                return "Error";
            }
        }
    }
}
