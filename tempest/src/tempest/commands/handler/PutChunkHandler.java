package tempest.commands.handler;

import tempest.commands.command.PutChunk;
import tempest.commands.interfaces.ResponseCommandExecutor;
import tempest.protos.Command;

import java.net.Socket;

/**
 * Created by swapnalekkala on 10/29/15.
 */
public class PutChunkHandler implements ResponseCommandExecutor<PutChunk, String, String> {
    public boolean canHandle(Command.Message.Type type) {
        return type == PutChunk.type;
    }

    public Command.Message serialize(PutChunk command) {
        Command.PutChunk.Builder putChunkBuilder = Command.PutChunk.newBuilder().setRequest(command.getRequest());

        if (command.getResponse() != null)
            putChunkBuilder.setResponse(command.getResponse());
        Command.Message message = Command.Message.newBuilder()
                .setType(Command.Message.Type.PUTCHUNK)
                .setPutChunk(putChunkBuilder)
                .build();
        return message;
    }

    public PutChunk deserialize(Command.Message message) {
        PutChunk putChunk = new PutChunk();
        putChunk.setRequest(message.getPutChunk().getRequest());
        if (message.hasPutChunk() && message.getPutChunk().hasResponse())
            putChunk.setResponse(message.getPutChunk().getResponse());
        return putChunk;
    }

    public String execute(Socket socket,String request) {

        //copy file to servers and return ok message
        // chunking
        return "Ok";
    }
}