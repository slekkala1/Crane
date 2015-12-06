package tempest.commands.handler;

import tempest.commands.command.Bolt;
import tempest.commands.command.Spout;
import tempest.commands.interfaces.ResponseCommand;
import tempest.commands.interfaces.ResponseCommandExecutor;
import tempest.protos.Command;
import tempest.protos.Membership;
import tempest.services.Tuple;
import tempest.services.bolt.BaseBolt;
import tempest.services.bolt.OutputCollector;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by swapnalekkala on 11/24/15.
 */
public class BoltHandler implements ResponseCommandExecutor<Bolt, String, String> {
    private Socket socket;
    LinkedBlockingQueue queue = new LinkedBlockingQueue();
    List<OutputCollector> outputCollectorList;

    public boolean canHandle(Command.Message.Type type) {
        return type == Bolt.type;
    }

    public Command.Message serialize(Bolt command) {
        Command.Bolt.Builder boltBuilder = Command.Bolt.newBuilder();
        if(command.getSendTupleTo()!=null) {
            boltBuilder.addAllSendTupleTo(command.getSendTupleTo());
        }
        if (command.getBoltType() != null) {
            boltBuilder.setBoltType(command.getBoltType());
        }
        // if(command.getParallelism()!= null) {
        //   boltBuilder.setParallelism(command.getParallelism());
        //}

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
        bolt.setSendTupleTo(message.getBolt().getSendTupleToList());
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
            //if (((Bolt)command).getSendTupleTo().isEmpty())
            for (Membership.Member member : ((Bolt)command).getSendTupleTo()) {
            	OutputCollector outputCollector = new OutputCollector(member);
            	outputCollectorList.add(outputCollector);
            }
            Command.Bolt.BoltType type = ((Bolt)command).getBoltType();
            int nThreads = ((Bolt)command).getParallelism();
            //}
            BaseBolt bolt = new BaseBolt(queue,outputCollectorList,type,nThreads);
            bolt.filter();
            thread.join();
            if (!((Bolt) command).getSendTupleTo().getHost().equals("")) {
                Thread outputThread = new Thread(outputCollector.emit());
                outputThread.start();
                outputThread.join();
            } else {
                Tuple tuple;
                while ((tuple = outputCollector.getQueue().poll(1000, TimeUnit.MILLISECONDS)) != null) {
                    ack(Integer.parseInt(tuple.getStringList().get(0)));
                    System.out.println(String.join(",", tuple.getStringList()));
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
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
                        if(((Bolt)command).getSendTupleTo().get(0).getHost().equals("")) {
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

    public void ack(int id) throws IOException {
        Ack ack = new Ack();
        ack.setId(id);
        String introducer = "fa15-cs425-g03-01.cs.illinois.edu:4444";
        Membership.Member member = Membership.Member.newBuilder()
              .setHost(introducer.split(":")[0])
            .setPort(Integer.parseInt(introducer.split(":")[1]))
          .build();
        createResponseExecutor(member, ack).execute();
    }

    private <TRequest, TResponse> ClientResponseCommandExecutor<TResponse> createResponseExecutor
            (Membership.Member member, ResponseCommand<TRequest, TResponse> command) throws IOException {
        ResponseCommandExecutor commandHandler = new AckHandler();
        String logFile = "machine." + Inet4Address.getLocalHost().getHostName() + ".log";
        Logger logger = new DefaultLogger(new CommandLineExecutor(), new DefaultLogWrapper(), logFile, logFile);
        return new TcpClientResponseCommandExecutor<>(member, command, commandHandler, logger);
    }
}
