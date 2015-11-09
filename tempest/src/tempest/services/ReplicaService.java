package tempest.services;

import tempest.commands.interfaces.CommandExecutor;
import tempest.commands.interfaces.ResponseCommandExecutor;
import tempest.interfaces.Logger;
import tempest.sdfs.client.SDFSClient;

import java.net.Inet4Address;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by swapnalekkala on 11/6/15.
 */
public class ReplicaService implements Runnable {
    private final Logger logger;
    private final CommandExecutor[] commandHandlers;
    private final ResponseCommandExecutor[] responseCommandHandlers;
    private Partitioner partitioner;
    private SDFSClient sdfsClient;
    private final ScheduledExecutorService scheduler;

    public ReplicaService(Logger logger, CommandExecutor[] commandHandlers,
                          ResponseCommandExecutor[] responseCommandHandlers, Partitioner partitioner, SDFSClient sdfsClient) {
        this.logger = logger;
        scheduler = Executors.newScheduledThreadPool(1);
        this.commandHandlers = commandHandlers;
        this.responseCommandHandlers = responseCommandHandlers;
        this.partitioner = partitioner;
        this.sdfsClient = sdfsClient;
    }

    public void start() {
        scheduler.scheduleAtFixedRate(this, 0, 5, TimeUnit.SECONDS);
    }

    public void stop() {
        scheduler.shutdown();
    }

    public void run() {
//        System.out.println("replica service started");
        for (Map.Entry<String, FileReplica> entry : this.partitioner.getFileAndReplicaMap().entrySet()) {
            String sDFSFileName = this.partitioner.getsDFSFileNamesAtTheVM().get(entry.getKey());
            logger.logLine(logger.INFO, "replica service for file " + entry.getKey());

            List<Integer> nodeKeyIds = this.partitioner.getServerListNodeIdsForChunkNoLocal(entry.getKey());
            logger.logLine(logger.INFO, "replica service for file " + nodeKeyIds);

            int localNodeId = this.partitioner.getLocalMachineNodeId();
            logger.logLine(logger.INFO, "replica service for file " + localNodeId);

            logger.logLine(logger.INFO, "is replica 1 alive? " + nodeKeyIds.contains(entry.getValue().getReplica1()));
            logger.logLine(logger.INFO, "is replica 2 alive? " + nodeKeyIds.contains(entry.getValue().getReplica2()));

            if (!nodeKeyIds.contains(entry.getValue().getReplica1()) && !nodeKeyIds.contains(entry.getValue().getReplica2())) {
                System.out.println("condition 1");
                byte[] byteArray = FileIOUtils.sendByteArraytoReplicate(entry.getKey());

                System.out.println("Replica Service send" + entry.getKey() + " to " + this.partitioner.getMachineByNodeId(nodeKeyIds.get(0)));
                this.sdfsClient.replicate(entry.getKey(), this.partitioner.getMachineByNodeId(nodeKeyIds.get(0)), localNodeId, nodeKeyIds.get(1), byteArray, sDFSFileName);

                System.out.println("Replica Service send" + entry.getKey() + " to " + this.partitioner.getMachineByNodeId(nodeKeyIds.get(1)));
                this.sdfsClient.replicate(entry.getKey(), this.partitioner.getMachineByNodeId(nodeKeyIds.get(1)), localNodeId, nodeKeyIds.get(0), byteArray, sDFSFileName);

                entry.getValue().setReplica1(nodeKeyIds.get(0));

                entry.getValue().setReplica2(nodeKeyIds.get(1));
            } else if (!nodeKeyIds.contains(entry.getValue().getReplica1()) && nodeKeyIds.contains(entry.getValue().getReplica2())) {
                System.out.println("condition 2");

                byte[] byteArray = FileIOUtils.sendByteArraytoReplicate(entry.getKey());
                //nodeKeyIds.remove(entry.getValue().getReplica2());
                for (int i = 0; i < nodeKeyIds.size(); i++) {
                    if (nodeKeyIds.get(i).equals(entry.getValue().getReplica2())) nodeKeyIds.remove(i);
                }

                System.out.println("Replica Service send" + entry.getKey() + " to " + this.partitioner.getMachineByNodeId(nodeKeyIds.get(0)));
                this.sdfsClient.replicate(entry.getKey(), this.partitioner.getMachineByNodeId(nodeKeyIds.get(0)), localNodeId, entry.getValue().getReplica2(), byteArray, sDFSFileName);

                entry.getValue().setReplica1(nodeKeyIds.get(0));
            } else if (nodeKeyIds.contains(entry.getValue().getReplica1()) && !nodeKeyIds.contains(entry.getValue().getReplica2())) {
                System.out.println("condition 3");

                byte[] byteArray = FileIOUtils.sendByteArraytoReplicate(entry.getKey());
                //nodeKeyIds.remove(entry.getValue().getReplica1());

                for (int i = 0; i < nodeKeyIds.size(); i++) {
                    if (nodeKeyIds.get(i).equals(entry.getValue().getReplica1())) nodeKeyIds.remove(i);
                }

                System.out.println("Replica Service send" + entry.getKey() + " to " + this.partitioner.getMachineByNodeId(nodeKeyIds.get(0)));
                this.sdfsClient.replicate(entry.getKey(), this.partitioner.getMachineByNodeId(nodeKeyIds.get(0)), localNodeId, entry.getValue().getReplica1(), byteArray, sDFSFileName);

                entry.getValue().setReplica2(nodeKeyIds.get(0));
            }
        }
    }
}
