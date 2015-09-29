package tempest.networking;

import tempest.commands.Header;
import tempest.interfaces.Command;
import tempest.interfaces.CommandHandler;
import tempest.interfaces.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;

public class UdpServiceWorker implements Runnable {
    private final DatagramPacket packet;
    private final DatagramSocket socket;
    private final CommandHandler[] commandHandlers;
    private final Logger logger;

    UdpServiceWorker(DatagramPacket packet, DatagramSocket socket, CommandHandler[] commandHandlers, Logger logger) {
        this.packet = packet;
        this.socket = socket;
        this.commandHandlers = commandHandlers;
        this.logger = logger;
    }

    public void run(){
        String[] data = new String(packet.getData(), 0, packet.getLength()).split("[\r\n]+");
        Header header = new Header(data[0]);
        String request = data[1];
        String response = Arrays.toString(Arrays.copyOfRange(data, 2, data.length));
        for (CommandHandler commandHandler : commandHandlers) {
            if (commandHandler.canHandle(header.getCommandId())) {
                Command command = commandHandler.deserialize(request, response);
                command.setResponse(commandHandler.execute(command.getRequest()));
                sendResponse(commandHandler.serialize(command));
            }
        }
    }

    private void sendResponse(String response) {
        byte[] data = response.getBytes();
        DatagramPacket responsePacket = new DatagramPacket(data, data.length, packet.getAddress(), packet.getPort());
        try {
            socket.send(responsePacket);
        } catch (IOException e) {
            logger.logLine(Logger.WARNING, "Couldn't send response packet.");
        }
    }
}
