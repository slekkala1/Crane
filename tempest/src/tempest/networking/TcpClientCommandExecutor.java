package tempest.networking;

import tempest.Machine;
import tempest.commands.Response;
import tempest.commands.ResponseData;
import tempest.interfaces.*;
import tempest.services.DefaultLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TcpClientCommandExecutor<TCommand extends Command<TRequest, TResponse>, TRequest, TResponse> implements ClientCommandExecutor<TResponse> {
    private final Machine server;
    private final TCommand command;
    private final CommandHandler<TCommand, TRequest, TResponse> commandHandler;
    private final Logger logger;

    public TcpClientCommandExecutor(Machine server, TCommand command, CommandHandler<TCommand, TRequest, TResponse> commandHandler, Logger logger) {
        this.server = server;
        this.command = command;
        this.commandHandler = commandHandler;
        this.logger = logger;
    }

    public Response<TResponse> call() {
        return execute();
    }

    public Response<TResponse> execute() {
        String line;
        long startTime = System.currentTimeMillis();
        try {
            Socket socket = new Socket(server.getHostName(), server.getPort());
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println(createHeader(command) + commandHandler.serialize(command));
            socket.shutdownOutput();

            in.readLine();
            String request = in.readLine();
            int lineCount = 0;
            StringBuilder responseBuilder = new StringBuilder();
            boolean first = true;
            while ((line = in.readLine()) != null) {
                if (!first) {
                    responseBuilder.append(System.lineSeparator());
                }
                first = false;
                responseBuilder.append(line);
                ++lineCount;
            }
            socket.close();

            Command<TRequest, TResponse> responseCommand = commandHandler.deserialize(request, responseBuilder.toString());
            Response<TResponse> result = new Response<>();
            result.setResponse(responseCommand.getResponse());
            result.setResponseData(new ResponseData(lineCount, System.currentTimeMillis() - startTime));
            return result;
        } catch (IOException e) {
            logger.logLine(DefaultLogger.WARNING, "Client socket failed while connecting to " + server + e);
            return null;
        }
    }

    private String createHeader(Command command) {
        return command.getCommandId() + System.lineSeparator();
    }
}