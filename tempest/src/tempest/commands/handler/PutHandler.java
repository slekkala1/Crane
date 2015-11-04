package tempest.commands.handler;

import tempest.commands.Response;
import tempest.commands.command.Put;
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
import java.util.ArrayList;

/**
 * Created by swapnalekkala on 10/27/15.
 */
public class PutHandler implements ResponseCommandExecutor<Put, String, String> {

    private Partitioner partitioner;

    public PutHandler() {
    }

    public PutHandler(Partitioner partitioner) {
        this.partitioner = partitioner;
    }

    public boolean canHandle(Command.Message.Type type) {
        return type == Put.type;
    }

    public Command.Message serialize(Put command) {
        Command.Put.Builder putBuilder = Command.Put.newBuilder().setRequest(command.getRequest()).setLocalFileName(command.getLocalFileName());

        if (command.getResponse() != null)
            putBuilder.setResponse(command.getResponse());
        Command.Message message = Command.Message.newBuilder()
                .setType(Command.Message.Type.PUT)
                .setPut(putBuilder)
                .build();
        return message;
    }

    public Put deserialize(Command.Message message) {
        Put put = new Put();
        put.setRequest(message.getPut().getRequest());
        put.setLocalFileName(message.getPut().getLocalFileName());
        if (message.hasPut() && message.getPut().hasResponse())
            put.setResponse(message.getPut().getResponse());
        return put;
    }

    public String execute(Socket socket,String request) {
        int CHUNK_SIZE = 6400000;//64kb
        chunkFile(request, CHUNK_SIZE);
        //copy file to servers and return ok message
        // chunking
        return "Ok";
    }

    public void chunkFile(String sDFSFileName, int CHUNK_SIZE) {
        File sDFSFile = new File(sDFSFileName);
        int FILE_SIZE = (int) sDFSFile.length();
        ArrayList<String> nameList = new ArrayList<String>();

        System.out.println("Total File Size: " + FILE_SIZE);
        int chunkId = 0;
        int NUMBER_OF_CHUNKS = FILE_SIZE/CHUNK_SIZE;
        if(FILE_SIZE%CHUNK_SIZE !=0) NUMBER_OF_CHUNKS = NUMBER_OF_CHUNKS + 1;
        byte[] temporary = null;

        try {
            InputStream inStream = null;
            int totalBytesRead = 0;

            inStream = new BufferedInputStream(new FileInputStream(sDFSFile));

            while (totalBytesRead < FILE_SIZE) {
                String PART_NAME = sDFSFileName + chunkId + ".bin";
                int bytesRemaining = FILE_SIZE - totalBytesRead;
                if (bytesRemaining < CHUNK_SIZE) // Remaining Data Part is Smaller Than CHUNK_SIZE
                // CHUNK_SIZE is assigned to remain volume
                {
                    CHUNK_SIZE = bytesRemaining;
                    System.out.println("CHUNK_SIZE: " + CHUNK_SIZE);
                }
                temporary = new byte[CHUNK_SIZE]; //Temporary Byte Array
                int bytesRead = inStream.read(temporary, 0, CHUNK_SIZE);

                Response TResponse = null;
                Membership.Member randomMember = null;
                boolean end = true;
                while (end) {

                    randomMember = partitioner.getServerToSendChunkTo(sDFSFileName);
                    System.out.println("sent to" + randomMember.getPort() + ":" + randomMember.getHost());

                    TResponse = putChunk(temporary, PART_NAME, sDFSFileName, chunkId, randomMember);
                    if(TResponse.getResponse().equals("Ok")) end = false;
                }
                partitioner.updateFileMetadata(sDFSFileName, PART_NAME, chunkId, CHUNK_SIZE, randomMember,NUMBER_OF_CHUNKS,FILE_SIZE);
                nameList.add(PART_NAME);

                if (bytesRead > 0) // If bytes read is not empty
                {
                    totalBytesRead += bytesRead;
                    chunkId++;
                }
//                putChunk(temporary, "D://" + PART_NAME);
//                nameList.add("D://" + PART_NAME);


                System.out.println("Total Bytes Read: " + totalBytesRead);
            }
            // return nameList;
        } catch (IOException e) {
            //logger.logLine(DefaultLogger.WARNING, "Client socket failed while connecting to " + server + e);
            //return null;
        }
        System.out.println(nameList.toString());
    }

    public Response putChunk(byte[] byteArray, String chunkName, String sDFSFileName, int chunkId, Membership.Member randomMember) throws IOException {
        PutChunk putChunk = new PutChunk();
        putChunk.setRequest(chunkName);
        putChunk.setByteArray(byteArray);
        return createResponseExecutor(randomMember, putChunk).execute();
    }

    private <TRequest, TResponse> ClientResponseCommandExecutor<TResponse> createResponseExecutor
            (Membership.Member member, ResponseCommand<TRequest, TResponse> command) throws IOException {
        ResponseCommandExecutor commandHandler = new PutChunkHandler();
        String logFile = "machine." + Inet4Address.getLocalHost().getHostName() + ".log";
        Logger logger = new DefaultLogger(new CommandLineExecutor(), new DefaultLogWrapper(), logFile, logFile);
        return new TcpClientResponseCommandExecutor<>(member, command, commandHandler, logger);
    }
}