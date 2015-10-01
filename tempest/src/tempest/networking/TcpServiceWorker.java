package tempest.networking;

import tempest.commands.Header;
import tempest.interfaces.Command;
import tempest.interfaces.CommandHandler;
import tempest.interfaces.Logger;
import tempest.services.DefaultLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TcpServiceWorker implements Runnable {
    private final Socket client;
    private final Logger logger;
    private final CommandHandler[] commandHandlers;

    TcpServiceWorker(Socket client, Logger logger, CommandHandler[] commandHandlers) {
        this.client = client;
        this.logger = logger;
        this.commandHandlers = commandHandlers;
    }

    public void run(){
        String line;
        BufferedReader in;
        PrintWriter out;
        try{
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            out = new PrintWriter(client.getOutputStream(), true);

            Header header = new Header(in.readLine());
            String request = in.readLine();
            StringBuilder builder = new StringBuilder();
            while ((line = in.readLine()) != null) {
                builder.append(line);
            }

            String response = builder.toString();
            for (CommandHandler commandHandler : commandHandlers) {
                if (commandHandler.canHandle(header.getCommandId())) {
                    Command command = commandHandler.deserialize(request, response);
                    command.setResponse(commandHandler.execute(command.getRequest()));
                    out.append(createHeader(command) + commandHandler.serialize(command));
                }
            }
            out.flush();
            out.close();

        } catch (IOException e) {
            logger.logLine(DefaultLogger.INFO, "Error handling command" + e);
        }
    }

    private String createHeader(Command command) {
        return command.getCommandId() + System.lineSeparator();
    }
}
