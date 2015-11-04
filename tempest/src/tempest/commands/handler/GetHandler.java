package tempest.commands.handler;

import tempest.commands.Response;
import tempest.commands.command.Get;
import tempest.commands.command.GetChunk;
import tempest.commands.command.Ping;
import tempest.commands.command.PutChunk;
import tempest.commands.interfaces.ResponseCommand;
import tempest.commands.interfaces.ResponseCommandExecutor;
import tempest.interfaces.ClientResponseCommandExecutor;
import tempest.interfaces.Logger;
import tempest.networking.TcpClientResponseCommandExecutor;
import tempest.protos.Command;
import tempest.protos.Membership;
import tempest.services.CommandLineExecutor;
import tempest.services.DefaultLogWrapper;
import tempest.services.DefaultLogger;
import tempest.services.Partitioner;

import java.io.*;
import java.net.Inet4Address;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * Created by swapnalekkala on 10/27/15.
 */
public class GetHandler implements ResponseCommandExecutor<Get, String, String> {
    private Partitioner partitioner;

    public GetHandler() {

    }

    public GetHandler(Partitioner partitioner) {
        this.partitioner = partitioner;
    }


    public boolean canHandle(Command.Message.Type type) {
        return type == Get.type;
    }


    public Command.Message serialize(Get command) {
        Command.Get.Builder getBuilder = Command.Get.newBuilder().setRequest(command.getRequest());
        if (command.getResponse() != null)
            getBuilder.setResponse(command.getResponse());
        Command.Message message = Command.Message.newBuilder()
                .setType(Command.Message.Type.GET)
                .setGet(getBuilder)
                .build();
        return message;
    }

    public Get deserialize(Command.Message message) {
        Get get = new Get();
        if (message.hasGet() && message.getGet().hasResponse())
            get.setResponse(message.getGet().getResponse());
        return get;
    }

    public String execute(Socket socket, String request) {

//get chunks from where they are stored to return to client
        //getFile(request);
        try {
            sendFile(socket.getOutputStream(), request);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Sent the file";
    }

    public void sendFile(OutputStream outputStream, String sDFSFileName) {

        byte AllFilesContent[] = null;

        int TOTAL_SIZE = 0;
        int NUMBER_OF_CHUNKS = this.partitioner.getNumberOfChunks(sDFSFileName);
        int CURRENT_LENGTH = 0;
        AllFilesContent = new byte[TOTAL_SIZE]; // Length of All Files, Total Size
        InputStream inStream = null;

        try {
            for (int i = 0; i < NUMBER_OF_CHUNKS; i++) {

                List<Membership.Member> serverList = this.partitioner.getServerListByChunkId(sDFSFileName, i);

                for (Membership.Member server : serverList) {
                    GetChunk getChunk = getChunk(server, this.partitioner.getChunkNameByChunkId(sDFSFileName, i),
                            this.partitioner.getChunkSizeByChunkId(sDFSFileName, i));
                    if (getChunk.getResponse().equals("Ok")) {
                        inStream = new ByteArrayInputStream(getChunk.getByteArray());
                        inStream.read(AllFilesContent, CURRENT_LENGTH, getChunk.getBytesSize());
                        CURRENT_LENGTH += getChunk.getBytesSize();
                        inStream.close();
                        break;
                    }
                }
            }

            assert AllFilesContent.length == this.partitioner.getTotalFilesize(sDFSFileName);
            outputStream.write(ByteBuffer.allocate(4).putInt(AllFilesContent.length).array());
            outputStream.write(AllFilesContent);

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Merge was executed successfully.!");
    }

    public GetChunk getChunk(Membership.Member server, String chunkName, int chunkSize) throws IOException {
        GetChunk getChunk = new GetChunk();
        getChunk.setRequest(chunkName);
        getChunk.setBytesSize(chunkSize);
        createResponseExecutor(server, getChunk).execute();
        return getChunk;
    }

    private <TRequest, TResponse> ClientResponseCommandExecutor<TResponse> createResponseExecutor
            (Membership.Member member, ResponseCommand<TRequest, TResponse> command) throws IOException {
        ResponseCommandExecutor commandHandler = new GetChunkHandler();
        String logFile = "machine." + Inet4Address.getLocalHost().getHostName() + ".log";
        Logger logger = new DefaultLogger(new CommandLineExecutor(), new DefaultLogWrapper(), logFile, logFile);
        return new TcpClientResponseCommandExecutor<>(member, command, commandHandler, logger);
    }


}