package tempest.networking;

import tempest.Machine;
import tempest.interfaces.CommandResponse;
import tempest.interfaces.Logger;
import tempest.interfaces.UdpClientCommand;
import tempest.services.DefaultLogger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.Callable;

public class UdpClientCommandExecutor<TResponse extends CommandResponse<TResponse>> implements Callable<TResponse> {
    private final Machine server;
    private final UdpClientCommand command;
    private final Logger logger;

    public UdpClientCommandExecutor(Machine server, UdpClientCommand command, Logger logger) {

        this.server = server;
        this.command = command;
        this.logger = logger;
    }

    public TResponse call() {
        return execute();
    }

    public TResponse execute() {
        long startTime = System.currentTimeMillis();
        try {
            DatagramSocket socket = new DatagramSocket(0);
            socket.setSoTimeout(500);

            byte[] requestData = command.getRequest().getBytes();
            DatagramPacket request = new DatagramPacket(requestData, requestData.length, InetAddress.getByName(server.getHostName()), server.getPort());
            DatagramPacket response = new DatagramPacket(new byte[1024], 1024);

            socket.send(request);
            socket.receive(response);

            String result = new String(response.getData());
            int lineCount = result.split(System.lineSeparator()).length;

            long stopTime = System.currentTimeMillis();
            long elapsedTime = stopTime - startTime;

            return (TResponse) command.getResponse(result, lineCount, elapsedTime);
        } catch (IOException e) {
            logger.logLine(DefaultLogger.WARNING, "Client failed " + server + e);
            return null;
        }
    }
}