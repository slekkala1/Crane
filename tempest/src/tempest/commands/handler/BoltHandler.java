package tempest.commands.handler;

import tempest.commands.command.Ack;
import tempest.commands.command.Bolt;
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
import tempest.services.Tuple;
import tempest.services.bolt.FilterBolt;
import tempest.services.bolt.OutputCollector;

import java.io.*;
import java.net.Inet4Address;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

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
        Command.Bolt.Builder boltBuilder = Command.Bolt.newBuilder()
                .setParallelism(command.getParallelism());
        if (command.getSendTupleTo() != null) {
            boltBuilder.setSendTupleTo(command.getSendTupleTo());
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
            //if (!((Bolt) command).getSendTupleTo().getHost().equals("")) {
            outputCollector = new OutputCollector(((Bolt) command).getSendTupleTo());
            //}
            //else {
            //  String introducer = "fa15-cs425-g03-01.cs.illinois.edu:4444";
            //Membership.Member member = Membership.Member.newBuilder()
            //      .setHost(introducer.split(":")[0])
            //    .setPort(Integer.parseInt(introducer.split(":")[1]))
            //  .build();

            //  outputCollector = new OutputCollector(member);
            //}
            if (((Bolt) command).getBoltType().toString().equals("FILTERBOLT")) {
                FilterBolt filterBolt = new FilterBolt(queue, outputCollector);
                filterBolt.filter();
            } else if (((Bolt) command).getBoltType().toString().equals("JOINBOLT")) {

            } else if (((Bolt) command).getBoltType().toString().equals("TRANSFORMBOLT")) {

            }
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
                        if (((Bolt) command).getSendTupleTo().getHost().equals("")) {
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
