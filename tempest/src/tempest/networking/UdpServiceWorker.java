package tempest.networking;

import tempest.interfaces.Logger;
import tempest.interfaces.ServerCommand;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UdpServiceWorker implements Runnable {
    private final DatagramPacket packet;
    private final DatagramSocket socket;
    private final ServerCommand[] commands;
    private final Logger logger;

    UdpServiceWorker(DatagramPacket packet, DatagramSocket socket, ServerCommand[] commands, Logger logger) {
        this.packet = packet;
        this.socket = socket;
        this.commands = commands;
        this.logger = logger;
    }

    public void run(){
        String request = new String(packet.getData(), 0, packet.getLength());
        for (ServerCommand command : commands) {
            if (command.canExecute(request)) {
                sendResponse(command.execute(request));
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
