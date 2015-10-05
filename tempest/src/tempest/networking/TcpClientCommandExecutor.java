package tempest.networking;

import tempest.commands.Response;
import tempest.commands.ResponseData;
import tempest.interfaces.*;
import tempest.interfaces.Command;
import tempest.protos.*;
import tempest.services.DefaultLogger;

import java.io.IOException;
import java.net.Socket;

public class TcpClientCommandExecutor<TCommand extends Command<TRequest, TResponse>, TRequest, TResponse> implements ClientCommandExecutor<TResponse> {
    private final Membership.Member server;
    private final TCommand command;
    private final CommandHandler<TCommand, TRequest, TResponse> commandHandler;
    private final Logger logger;

    public TcpClientCommandExecutor(Membership.Member server, TCommand command, CommandHandler<TCommand, TRequest, TResponse> commandHandler, Logger logger) {
        this.server = server;
        this.command = command;
        this.commandHandler = commandHandler;
        this.logger = logger;
    }

    public Response<TResponse> call() {
        return execute();
    }

    public Response<TResponse> execute() {
        long startTime = System.currentTimeMillis();
        try {
            Socket socket = new Socket(server.getHost(), server.getPort());

            commandHandler.serialize(command).writeTo(socket.getOutputStream());
            socket.shutdownOutput();

            tempest.protos.Command.Message message = tempest.protos.Command.Message.parseFrom(socket.getInputStream());
            socket.close();

            Command<TRequest, TResponse> responseCommand = commandHandler.deserialize(message);
            Response<TResponse> result = new Response<>();
            result.setResponse(responseCommand.getResponse());
            result.setResponseData(new ResponseData(System.currentTimeMillis() - startTime));
            return result;
        } catch (IOException e) {
            logger.logLine(DefaultLogger.WARNING, "Client socket failed while connecting to " + server + e);
            return null;
        }
    }
}