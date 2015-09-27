package tempest.networking;

import tempest.interfaces.Logger;
import tempest.interfaces.ServerCommand;
import tempest.services.DefaultLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TcpServiceWorker implements Runnable {
    private final Socket client;
    private final Logger logger;
    private final ServerCommand[] commands;

    TcpServiceWorker(Socket client, Logger logger, ServerCommand[] commands) {
        this.client = client;
        this.logger = logger;
        this.commands = commands;
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
            logger.logLine(DefaultLogger.INFO, "Error handling command" + e);
        }
    }
}
