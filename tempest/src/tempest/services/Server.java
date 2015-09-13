package tempest.services;

import tempest.commands.server.Grep;
import tempest.commands.server.Ping;
import tempest.interfaces.ServerCommand;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private final ServerCommand[] commands;
    private final Logger logger;
    private final int port;
    private ServiceRunner runner;

    public Server(Logger logger, int port) {
        this.logger = logger;
        this.port = port;
        commands = new ServerCommand[] { new Ping(), new Grep(logger)};
    }

    public void start() {
        if (runner != null)
            return;
        runner = new ServiceRunner();
        new Thread(runner).start();
    }

    public void stop() {
        if (runner == null)
            return;
        runner.stop();
        runner = null;
    }

    class ServiceRunner implements Runnable {
        private boolean isRunning = true;
        private ServerSocket server;

        public void run() {
            try{
                server = new ServerSocket(port);

                while(isRunning){
                    ServiceWorker worker;
                    worker = new ServiceWorker(server.accept());
                    new Thread(worker).start();
                }
            } catch (IOException e) {
                if (!isRunning)
                    return;
                logger.logLine(Logger.INFO, "Error accepting client request" + e);
            }
        }

        public void stop() {
            isRunning = false;
            if (server == null)
                return;
            try {
                server.close();
            } catch (IOException ignored) {
            }
        }
    }

    class ServiceWorker implements Runnable {
        private final Socket client;

        ServiceWorker(Socket client) {
            this.client = client;
        }

        public void run(){
            String line;
            BufferedReader in;
            PrintWriter out;
            try{
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                out = new PrintWriter(client.getOutputStream(), true);

                StringBuilder builder = new StringBuilder();
                while ((line = in.readLine()) != null) {
                    builder.append(line);
                }

                String request = builder.toString();
                for (ServerCommand command : commands) {
                    if (command.canExecute(request)) {
                        out.append(command.execute(request));
                    }
                }
                out.flush();
                out.close();

            } catch (IOException e) {
                logger.logLine(Logger.INFO, "Error handling command" + e);
            }
        }
    }
}
