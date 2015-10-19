package tempest.networking;

import com.google.protobuf.InvalidProtocolBufferException;
import tempest.commands.interfaces.*;
import tempest.interfaces.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UdpServiceWorker implements Runnable {
    private final DatagramPacket packet;
    private final DatagramSocket socket;
    private final CommandExecutor[] commandHandlers;
    private final ResponseCommandExecutor[] responseCommandHandlers;
    private final Logger logger;

    UdpServiceWorker(DatagramPacket packet, DatagramSocket socket, CommandExecutor[] commandHandlers, ResponseCommandExecutor[] responseCommandHandlers, Logger logger) {
        this.packet = packet;
        this.socket = socket;
        this.commandHandlers = commandHandlers;
        this.responseCommandHandlers = responseCommandHandlers;
        this.logger = logger;
    }

    public void run(){
        tempest.protos.Command.Message message;
        try {
            message = tempest.protos.Command.Message.parseFrom(packet.getData());
            for (CommandExecutor commandHandler : commandHandlers) {
                if (commandHandler.canHandle(message.getType())) {
                    ResponseCommand command = (ResponseCommand)commandHandler.deserialize(message);
                    commandHandler.execute(command.getRequest());
                }
            }
            for (ResponseCommandExecutor commandHandler : responseCommandHandlers) {
                if (commandHandler.canHandle(message.getType())) {
                    ResponseCommand command = (ResponseCommand)commandHandler.deserialize(message);
                    command.setResponse(commandHandler.execute(command.getRequest()));
                    sendResponse(commandHandler.serialize(command));
                }
            }
        } catch (InvalidProtocolBufferException e) {
            logger.logLine(Logger.SEVERE, "Unable to deserialize message");
        }
    }

    private void sendResponse(tempest.protos.Command.Message message) {
        byte[] data = message.toByteArray();
        DatagramPacket responsePacket = new DatagramPacket(data, data.length, packet.getAddress(), packet.getPort());
        try {
            socket.send(responsePacket);
        } catch (IOException e) {
            logger.logLine(Logger.WARNING, "Couldn't send response packet.");
        }
    }
}
