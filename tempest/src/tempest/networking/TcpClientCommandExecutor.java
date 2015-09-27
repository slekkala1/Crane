package tempest.networking;

import tempest.Machine;
import tempest.interfaces.ClientCommand;
import tempest.interfaces.ClientCommandExecutor;
import tempest.interfaces.CommandResponse;
import tempest.interfaces.Logger;
import tempest.services.DefaultLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TcpClientCommandExecutor<TResponse extends CommandResponse<TResponse>> implements ClientCommandExecutor<TResponse> {
    private final Machine server;
    private final ClientCommand command;
    private final Logger logger;

    public TcpClientCommandExecutor(Machine server, ClientCommand command, Logger logger) {

        this.server = server;
        this.command = command;
        this.logger = logger;
    }

    public TResponse call() {
        return execute();
    }

    public TResponse execute() {
        String line;
        long startTime = System.currentTimeMillis();
        try {
            Socket socket = new Socket(server.getHostName(), server.getPort());
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println(command.getRequest());
            socket.shutdownOutput();

            int lineCount = 0;
            StringBuilder builder = new StringBuilder();
            while ((line = in.readLine()) != null) {
                builder.append(line).append(System.lineSeparator());
                ++lineCount;
            }
            socket.close();
            long stopTime = System.currentTimeMillis();
            long elapsedTime = stopTime - startTime;
            return (TResponse) command.getResponse(builder.toString(), lineCount, elapsedTime);
        } catch (IOException e) {
            logger.logLine(DefaultLogger.WARNING, "Client socket failed while connecting to " + server + e);
            return null;
        }
    }
}