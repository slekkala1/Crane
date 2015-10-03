package tempest.networking;

import tempest.interfaces.CommandHandler;
import tempest.interfaces.Logger;
import tempest.services.DefaultLogger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UdpServiceRunner implements Runnable {
    private final Logger logger;
    private final int port;
    private boolean isRunning = true;
    private DatagramSocket server;
    private final CommandHandler[] commandHandlers;

    public UdpServiceRunner(Logger logger, int port, CommandHandler[] commandHandlers) {
        this.logger = logger;
        this.port = port;
        this.commandHandlers = commandHandlers;
    }

    public void run() {
        try{
            server = new DatagramSocket(port);

            while(isRunning){
                UdpServiceWorker worker;
                byte[] data = new byte[1024];
                DatagramPacket packet = new DatagramPacket(data, data.length);
                server.receive(packet);
                worker = new UdpServiceWorker(packet, server, commandHandlers, logger);
                new Thread(worker).start();
            }
        } catch (IOException e) {
            if (!isRunning)
                return;
            logger.logLine(DefaultLogger.INFO, "Error accepting client request" + e);
        }
    }

    public void stop() {
        isRunning = false;
        if (server == null)
            return;
        server.close();
    }
}