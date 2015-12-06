package tempest.commands.handler;

import tempest.commands.command.Bolt;
import tempest.commands.command.Spout;
import tempest.commands.interfaces.ResponseCommand;
import tempest.commands.interfaces.ResponseCommandExecutor;
import tempest.protos.Command;
import tempest.services.Tuple;
import tempest.services.bolt.FilterBolt;
import tempest.services.bolt.OutputCollector;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by swapnalekkala on 11/24/15.
 */
public class BoltHandler implements ResponseCommandExecutor<Bolt, String, String> {
    private Socket socket;
    LinkedBlockingQueue queue = new LinkedBlockingQueue();
    OutputCollector outputCollector;

    public boolean canHandle(Command.Message.Type type) {
        return type == Bolt.type;
    }

    public Command.Message serialize(Bolt command) {
        Command.Bolt.Builder boltBuilder = Command.Bolt.newBuilder();
        if(command.getSendTupleTo()!=null) {
            boltBuilder.setSendTupleTo(command.getSendTupleTo());
        }
        if (command.getResponse() != null)
            boltBuilder.setResponse(command.getResponse());
        Command.Message message = Command.Message.newBuilder()
                .setType(Command.Message.Type.BOLT)
                .setBolt(boltBuilder)
                .build();
        return message;
    }

    public Bolt deserialize(Command.Message message) {
        Bolt bolt = new Bolt();
        bolt.setSendTupleTo(message.getBolt().getSendTupleTo());
        if (message.hasBolt() && message.getBolt().hasResponse())
            bolt.setResponse(message.getBolt().getResponse());
        return bolt;
    }

    public String execute(Socket socket, ResponseCommand<String, String> command) {

        try {
            this.socket = socket;
            Thread thread = new Thread(readTuples(command));
            thread.start();
           // if(!((Bolt)command).getSendTupleTo().getHost().equals("")) {
            outputCollector = new OutputCollector(((Bolt) command).getSendTupleTo());
            //}
            FilterBolt filterBolt = new FilterBolt(queue,outputCollector);
            filterBolt.filter();
            thread.join();
            if(!((Bolt)command).getSendTupleTo().getHost().equals("")) {
                Thread outputThread = new Thread(outputCollector.emit());
                outputThread.start();
                outputThread.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//        BoltService
        return "ok";
    }

    public Runnable readTuples(ResponseCommand<String, String> command) {
        return new Runnable() {
            @Override
            public void run() {
                boolean read = true;

                try {
                    ObjectInputStream objectInput = new ObjectInputStream(socket.getInputStream());
                    while (read) {
                        Integer tupleSize = objectInput.readInt();
                        if (tupleSize == -1) {
                            break;
                        }
                        byte[] b = new byte[tupleSize];
                        objectInput.readFully(b, 0, tupleSize);

                        ByteArrayInputStream in = new ByteArrayInputStream(b);
                        ObjectInputStream is = new ObjectInputStream(in);
                        Tuple tuple = (Tuple) is.readObject();
                        queue.add(tuple);
                        //filterBolt.filter();
                        //System.out.println(String.join(",", tuple.getStringList()));
                        if(((Bolt)command).getSendTupleTo().getHost().equals("")) {
                            System.out.println(String.join(",", tuple.getStringList()));
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }

        };
    }
}
