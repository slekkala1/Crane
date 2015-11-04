package tempest.commands.handler;

import tempest.commands.command.GetChunk;
import tempest.commands.interfaces.ResponseCommandExecutor;
import tempest.protos.Command;

import java.io.*;
import java.net.Socket;

/**
 * Created by swapnalekkala on 11/1/15.
 */
public class GetChunkHandler implements ResponseCommandExecutor<GetChunk, String, String> {
    public boolean canHandle(Command.Message.Type type) {
        return type == GetChunk.type;
    }

    public Command.Message serialize(GetChunk command) {
        Command.GetChunk.Builder getChunkBuilder = Command.GetChunk.newBuilder().setRequest(command.getRequest());

        if (command.getResponse() != null)
            getChunkBuilder.setResponse(command.getResponse());
        Command.Message message = Command.Message.newBuilder()
                .setType(Command.Message.Type.GETCHUNK)
                .setGetChunk(getChunkBuilder)
                .build();
        return message;
    }

    public GetChunk deserialize(Command.Message message) {
        GetChunk getChunk = new GetChunk();
        getChunk.setRequest(message.getGetChunk().getRequest());
        if (message.hasGetChunk() && message.getGetChunk().hasResponse())
            getChunk.setResponse(message.getGetChunk().getResponse());
        return getChunk;
    }

    public String execute(Socket socket,String request) {
        sendChunk(socket, request);
        return "Ok";
    }

    public void sendChunk(Socket socket, String chunkName) {
        BufferedOutputStream outToServer = null;

        try {
            outToServer = new BufferedOutputStream(socket.getOutputStream());
        } catch (IOException ex) {
            // Do exception handling
        }

        if (outToServer != null) {
            File myFile = new File(chunkName);
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

}