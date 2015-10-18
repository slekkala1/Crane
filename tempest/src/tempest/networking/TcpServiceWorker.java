package tempest.networking;

import tempest.commands.interfaces.ResponseCommand;
import tempest.commands.interfaces.ResponseCommandExecutor;
import tempest.interfaces.Logger;
import tempest.services.DefaultLogger;

import java.io.*;
import java.net.Socket;

public class TcpServiceWorker implements Runnable {
    private final Socket client;
    private final Logger logger;
    private final ResponseCommandExecutor[] commandHandlers;

    TcpServiceWorker(Socket client, Logger logger, ResponseCommandExecutor[] commandHandlers) {
        this.client = client;
        this.logger = logger;
        this.commandHandlers = commandHandlers;
    }

    public void run(){
        try{
            tempest.protos.Command.Message message = tempest.protos.Command.Message.parseFrom(client.getInputStream());

            for (ResponseCommandExecutor commandHandler : commandHandlers) {
                if (commandHandler.canHandle(message.getType())) {
                    ResponseCommand command = (ResponseCommand)commandHandler.deserialize(message);
                    command.setResponse(commandHandler.execute(command.getRequest()));
                    commandHandler.serialize(command).writeTo(client.getOutputStream());
                }
            }
            client.getOutputStream().flush();
            client.getOutputStream().close();

        } catch (IOException e) {
            logger.logLine(DefaultLogger.INFO, "Error handling command" + e);
        }
    }
}
