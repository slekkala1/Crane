package tempest.networking;

import tempest.interfaces.Command;
import tempest.interfaces.CommandHandler;
import tempest.interfaces.Logger;
import tempest.services.DefaultLogger;

import java.io.*;
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
        PrintWriter out;
        try{
            tempest.protos.Command.Message message = tempest.protos.Command.Message.parseFrom(client.getInputStream());
            out = new PrintWriter(client.getOutputStream(), true);

            for (CommandHandler commandHandler : commandHandlers) {
                if (commandHandler.canHandle(message.getType())) {
                    Command command = commandHandler.deserialize(message);
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
        return command.getType() + System.lineSeparator();
    }
}
