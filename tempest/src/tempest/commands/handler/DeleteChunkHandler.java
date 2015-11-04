package tempest.commands.handler;

import tempest.commands.command.DeleteChunk;
import tempest.commands.interfaces.ResponseCommandExecutor;
import tempest.protos.Command;

import java.io.File;
import java.net.Socket;

/**
 * Created by swapnalekkala on 11/1/15.
 */
public class DeleteChunkHandler implements ResponseCommandExecutor<DeleteChunk, String, String> {
    public boolean canHandle(Command.Message.Type type) {
        return type == DeleteChunk.type;
    }

    public Command.Message serialize(DeleteChunk command) {
        Command.DeleteChunk.Builder deleteChunkBuilder = Command.DeleteChunk.newBuilder().setRequest(command.getRequest());

        if (command.getResponse() != null)
            deleteChunkBuilder.setResponse(command.getResponse());
        Command.Message message = Command.Message.newBuilder()
                .setType(Command.Message.Type.DELETECHUNK)
                .setDeleteChunk(deleteChunkBuilder)
                .build();
        return message;
    }

    public DeleteChunk deserialize(Command.Message message) {
        DeleteChunk deleteChunk = new DeleteChunk();
        deleteChunk.setRequest(message.getDeleteChunk().getRequest());
        if (message.hasDeleteChunk() && message.getDeleteChunk().hasResponse())
            deleteChunk.setResponse(message.getDeleteChunk().getResponse());
        return deleteChunk;
    }

    public String execute(Socket socket,String request) {
        deleteChunk(request);
        return "Ok";
    }

    public void deleteChunk(String chunkName) {
        try{
            File file = new File(chunkName);

            if(file.delete()){
                System.out.println(file.getName() + " is deleted!");
            }else{
                System.out.println("Delete operation is failed.");
            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }

}