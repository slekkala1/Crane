package tempest.networking;

import tempest.commands.handler.GetHandler;
import tempest.commands.interfaces.ResponseCommand;
import tempest.commands.interfaces.ResponseCommandExecutor;
import tempest.interfaces.Logger;
import tempest.protos.Command;
import tempest.services.DefaultLogger;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;

public class TcpServiceWorker implements Runnable {
    private final Socket client;
    private final Logger logger;
    private final ResponseCommandExecutor[] commandHandlers;

    TcpServiceWorker(Socket client, Logger logger, ResponseCommandExecutor[] commandHandlers) {
        this.client = client;
        this.logger = logger;
        this.commandHandlers = commandHandlers;
    }

    public void run() {
        try {
            byte[] commandLength = new byte[4];
            int read = client.getInputStream().read(commandLength);
            byte[] commandBytes = new byte[ByteBuffer.wrap(commandLength).getInt()];
            client.getInputStream().read(commandBytes);
            tempest.protos.Command.Message message = tempest.protos.Command.Message.parseFrom(commandBytes);

            for (ResponseCommandExecutor commandHandler : commandHandlers) {
                if (commandHandler.canHandle(message.getType())) {
                    ResponseCommand command = (ResponseCommand) commandHandler.deserialize(message);
                    if (message.getType() == Command.Message.Type.PUT || message.getType() == Command.Message.Type.PUTCHUNK) {
                        writeFileToDisk(client.getInputStream(), (String) command.getRequest());
                    }

                    command.setResponse(commandHandler.execute(client,command.getRequest()));
                    commandHandler.serialize(command).writeTo(client.getOutputStream());
                }
            }
            client.getOutputStream().flush();
            client.getOutputStream().close();

        } catch (IOException e) {
            logger.logLine(DefaultLogger.INFO, "Error handling command" + e);
        }
    }

    public void writeFileToDisk(InputStream in, String sDFSfileName) {
        byte[] aByte = new byte[1];
        int bytesRead;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        if (in != null) {

            FileOutputStream fos = null;
            BufferedOutputStream bos = null;
            try {
                fos = new FileOutputStream(new File(sDFSfileName));
                bos = new BufferedOutputStream(fos);
                bytesRead = in.read(aByte, 0, aByte.length);

                do {
                    baos.write(aByte);
                    bytesRead = in.read(aByte);
                } while (bytesRead != -1);

                bos.write(baos.toByteArray());
                bos.flush();
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}



