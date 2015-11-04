package tempest.networking;

import tempest.commands.Response;
import tempest.commands.ResponseData;
import tempest.commands.command.Get;
import tempest.commands.command.GetChunk;
import tempest.commands.command.Put;
import tempest.commands.command.PutChunk;
import tempest.commands.handler.PutHandler;
import tempest.commands.interfaces.ResponseCommand;
import tempest.commands.interfaces.ResponseCommandExecutor;
import tempest.interfaces.*;
import tempest.protos.*;
import tempest.services.DefaultLogger;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;

public class TcpClientResponseCommandExecutor<TCommand extends ResponseCommand<TRequest, TResponse>, TRequest, TResponse> implements ClientResponseCommandExecutor<TResponse> {
    private final Membership.Member server;
    private final TCommand command;
    private final ResponseCommandExecutor<TCommand, TRequest, TResponse> commandHandler;
    private final Logger logger;

    public TcpClientResponseCommandExecutor(Membership.Member server, TCommand command, ResponseCommandExecutor<TCommand, TRequest, TResponse> commandHandler, Logger logger) {
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
            Socket socket = new Socket(server.getHost(), server.getPort());
            Command.Message serializedCommand = commandHandler.serialize(command);
            socket.getOutputStream().write(ByteBuffer.allocate(4).putInt(serializedCommand.toByteArray().length).array());
            serializedCommand.writeTo(socket.getOutputStream());

            if (command instanceof Put) {
                sendFile(socket, (String) ((Put) command).getLocalFileName());
            }
            if (command instanceof PutChunk) {
                sendFileChunk(socket, ((PutChunk) command).getByteArray());
            }
            socket.isOutputShutdown();
            socket.shutdownOutput();
            if (command instanceof Get) {
                byte[] commandLength = new byte[4];
                int read = socket.getInputStream().read(commandLength);
                byte[] commandBytes = new byte[ByteBuffer.wrap(commandLength).getInt()];
//                socket.getInputStream().read(commandBytes);
                writeFileToDisk(socket.getInputStream(), (String) command.getRequest(),commandBytes);
            }

            if (command instanceof GetChunk) {
                byte[] b = null;
                int bytesSize = socket.getInputStream().read(b, 0,(int) ((GetChunk) command).getBytesSize());
                ((GetChunk) command).setByteArray(b);
            }

            tempest.protos.Command.Message message = tempest.protos.Command.Message.parseFrom(socket.getInputStream());
            socket.close();

            ResponseCommand<TRequest, TResponse> responseCommand = commandHandler.deserialize(message);
            Response<TResponse> result = new Response<>();
            result.setResponse(responseCommand.getResponse());
            result.setResponseData(new ResponseData(System.currentTimeMillis() - startTime));
            return result;
        } catch (IOException e) {
            logger.logLine(DefaultLogger.WARNING, "Client socket failed while connecting to " + server + e);
            return null;
        }
    }


    public void sendFile(Socket socket, String fileName) {
        BufferedOutputStream outToServer = null;

        try {
            outToServer = new BufferedOutputStream(socket.getOutputStream());
        } catch (IOException ex) {
            // Do exception handling
        }

        if (outToServer != null) {
            File myFile = new File(fileName);
            byte[] mybytearray = new byte[(int) myFile.length()];

            FileInputStream fis = null;

            try {
                fis = new FileInputStream(myFile);
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }
            BufferedInputStream bis = new BufferedInputStream(fis);

            try {
                bis.read(mybytearray, 0, mybytearray.length);
                outToServer.write(mybytearray, 0, mybytearray.length);
                outToServer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // File sent, exit the main method
            return;
        }
    }

    public void sendFileChunk(Socket socket, byte[] byteArray) {
        BufferedOutputStream outToServer = null;

        try {
            outToServer = new BufferedOutputStream(socket.getOutputStream());
        } catch (IOException ex) {
            // Do exception handling
        }

        if (outToServer != null) {
            try {
             //   bis.read(mybytearray, 0, mybytearray.length);
                outToServer.write(byteArray);
                outToServer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // File sent, exit the main method
            return;
        }
    }

    public void writeFileToDisk(InputStream in, String sDFSfileName,byte[] commandBytes) {
        byte[] aByte = new byte[commandBytes.length];
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
                } while (bytesRead != commandBytes.length);

                bos.write(baos.toByteArray());
                bos.flush();
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}