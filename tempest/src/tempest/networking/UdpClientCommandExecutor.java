package tempest.networking;

import tempest.Machine;
import tempest.commands.Response;
import tempest.commands.ResponseData;
import tempest.interfaces.*;
import tempest.services.DefaultLogger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

public class UdpClientCommandExecutor<TCommand extends Command<TRequest, TResponse>, TRequest, TResponse> implements ClientCommandExecutor<TResponse> {
    private final Machine server;
    private final TCommand command;
    private final CommandHandler<TCommand, TRequest, TResponse> commandHandler;
    private final Logger logger;

    public UdpClientCommandExecutor(Machine server, TCommand command, CommandHandler<TCommand, TRequest, TResponse> commandHandler, Logger logger) {

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
            DatagramSocket socket = new DatagramSocket(0);
            socket.setSoTimeout(500);

            byte[] requestData = (createHeader(command) + commandHandler.serialize(command)).getBytes();
            DatagramPacket udpRequest = new DatagramPacket(requestData, requestData.length, InetAddress.getByName(server.getHostName()), server.getPort());
            DatagramPacket udpResponse = new DatagramPacket(new byte[1024], 1024);

            socket.send(udpRequest);
            socket.receive(udpResponse);

            String[] data = new String(udpResponse.getData(), 0, udpResponse.getLength()).split("[\r\n]+");
            String request = data[1];
            String response = Arrays.toString(Arrays.copyOfRange(data, 2, data.length));
            Command<TRequest, TResponse> responseCommand = commandHandler.deserialize(request, response);

            int lineCount = data.length - 2;
            long stopTime = System.currentTimeMillis();
            long elapsedTime = stopTime - startTime;

            Response<TResponse> result = new Response<>();
            result.setResponse(responseCommand.getResponse());
            result.setResponseData(new ResponseData(lineCount, elapsedTime));

            return result;
        } catch (IOException e) {
            logger.logLine(DefaultLogger.WARNING, "Client failed " + server + e);
            return null;
        }
    }

    private String createHeader(Command command) {
        return command.getResponse() + System.lineSeparator();
    }
}