package tempest.networking;

import tempest.commands.Response;
import tempest.commands.ResponseData;
import tempest.interfaces.*;
import tempest.interfaces.Command;
import tempest.protos.*;
import tempest.services.DefaultLogger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UdpClientCommandExecutor<TCommand extends Command<TRequest, TResponse>, TRequest, TResponse> implements ClientCommandExecutor<TResponse> {
    private final Membership.Member server;
    private final TCommand command;
    private final CommandHandler<TCommand, TRequest, TResponse> commandHandler;
    private final Logger logger;

    public UdpClientCommandExecutor(Membership.Member server, TCommand command, CommandHandler<TCommand, TRequest, TResponse> commandHandler, Logger logger) {

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

            byte[] requestData = commandHandler.serialize(command).toByteArray();
            DatagramPacket udpRequest = new DatagramPacket(requestData, requestData.length, InetAddress.getByName(server.getHost()), server.getPort());
            DatagramPacket udpResponse = new DatagramPacket(new byte[1024], 1024);

            socket.send(udpRequest);
            socket.receive(udpResponse);

            tempest.protos.Command.Message message = tempest.protos.Command.Message.parseFrom(udpResponse.getData());
            Command<TRequest, TResponse> responseCommand = commandHandler.deserialize(message);

            long stopTime = System.currentTimeMillis();
            long elapsedTime = stopTime - startTime;

            Response<TResponse> result = new Response<>();
            result.setResponse(responseCommand.getResponse());
            result.setResponseData(new ResponseData(elapsedTime));

            return result;
        } catch (IOException e) {
            logger.logLine(DefaultLogger.WARNING, "Client failed " + server + e);
            return null;
        }
    }
}