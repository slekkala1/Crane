package tempest.services;

import org.apache.commons.lang3.tuple.ImmutablePair;
import tempest.interfaces.Logger;
import tempest.protos.Membership;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by swapnalekkala on 10/30/15.
 */
public class Partitioner {

    private final List<String> allMachines = new ArrayList<String>() {{
        add("fa15-cs425-g03-01.cs.illinois.edu:4444");
        add("fa15-cs425-g03-02.cs.illinois.edu:4444");
        add("fa15-cs425-g03-03.cs.illinois.edu:4444");
        add("fa15-cs425-g03-04.cs.illinois.edu:4444");
        add("fa15-cs425-g03-05.cs.illinois.edu:4444");
        add("fa15-cs425-g03-06.cs.illinois.edu:4444");
        add("fa15-cs425-g03-07.cs.illinois.edu:4444");
    }};

    /*  private List<String> allMachines = new ArrayList<String>() {{
          add("swapnas-MacBook-Air.local:4444");
      }};
*/
    private final Logger logger;
    private final MembershipService membershipService;
    private final Map<Integer, String> allMachinesId = new HashMap<Integer, String>();
    private final Map<String, String> sDFSFileNamesAtTheVM = new HashMap<>();
    private final Map<String, FileReplica> replicas = new HashMap<String, FileReplica>();
    private final Object lock = new Object();

    // private Map<ImmutablePair<String, Integer>, Chunk> metadata = new HashMap<ImmutablePair<String, Integer>, Chunk>();

    public Partitioner(Logger logger, MembershipService membershipService) {
        this.membershipService = membershipService;
        this.logger = logger;
        getAllServerNodeIds();
    }

    public void addFileAndReplicas(String sDFSFileChunkName, FileReplica fileReplica) {
        synchronized (lock) {
            System.out.println("In addFileAndReplicas method in Partitioner");
            this.replicas.put(sDFSFileChunkName, fileReplica);
        }
    }

    public Map<String, FileReplica> getFileAndReplicaMap() {
        synchronized (lock) {
            //logger.logLine(logger.INFO,"In getFileAndReplicaMap method in Partitioner");
            return this.replicas;
        }
    }

    public List<Membership.Member> getAllServersThatAreAlive() {
        return this.membershipService.getMembershipList().getMemberList();
    }

    public void getAllServerNodeIds() {
        for (int i = 0; i < allMachines.size(); i++) {
            allMachinesId.put(HashKey.hexToKey(HashKey.hashKey(allMachines.get(i))),
                    allMachines.get(i));
        }
    }

    public Map<String, String> getsDFSFileNamesAtTheVM() {
        return this.sDFSFileNamesAtTheVM;
    }

    public String getLocalHostName() {
        try {
            return Inet4Address.getLocalHost().getHostName().toString();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return "";
    }

    public void setsDFSFileNamesAtTheVM(String addedSDFSFile, String sDFSFileName) {
        this.sDFSFileNamesAtTheVM.put(addedSDFSFile, sDFSFileName);
    }

    public Membership.Member getServerToSendChunkTo(String sDFSFileName) {
        int fileKey = HashKey.hexToKey(HashKey.hashKey(sDFSFileName));
        System.out.println("fileKey for " + sDFSFileName + "is " + fileKey);
        logger.logLine(Logger.INFO, "fileKey for " + sDFSFileName + "is " + fileKey);

        int nodeKey = -1;
        List<Integer> aliveIds = new ArrayList<Integer>();

        for (Membership.Member member : this.membershipService.getMembershipList().getMemberList()) {
            aliveIds.add(HashKey.hexToKey(HashKey.hashKey(member.getHost() + ":" + member.getPort())));
            System.out.println(HashKey.hexToKey(HashKey.hashKey(member.getHost() + ":" + member.getPort())));
        }

        Collections.sort(aliveIds);
        for (Integer i : aliveIds) {
            if (fileKey <= i) {
                nodeKey = i;
                break;
            }
        }

        if (nodeKey == -1 && aliveIds.size() >= 1) {
            nodeKey = aliveIds.get(0);
        }


        System.out.println("aliveIds" + nodeKey);
        logger.logLine(Logger.INFO, "aliveIds" + nodeKey);


        System.out.println(allMachinesId.get(nodeKey));
        logger.logLine(Logger.INFO, "machine" + allMachinesId.get(nodeKey) + "for nodeKey" +
                nodeKey + "for SDFS file name" + sDFSFileName);


        return Membership.Member.newBuilder().
                setPort(Integer.parseInt(allMachinesId.get(nodeKey).split(":")[1]))
                .setHost(allMachinesId.get(nodeKey).split(":")[0]).build();
    }

    public List<Membership.Member> getServerListForChunk(String sDFSFileName) {
        int fileKey = HashKey.hexToKey(HashKey.hashKey(sDFSFileName));
        List<Integer> aliveIds = new ArrayList<Integer>();

        for (Membership.Member member : this.membershipService.getMembershipList().getMemberList()) {
            aliveIds.add(HashKey.hexToKey(HashKey.hashKey(member.getHost() + ":" + member.getPort())));
        }

        List<Integer> nodeKeyIds = new ArrayList<Integer>();

        Collections.sort(aliveIds);
        for (int i = 0; i < aliveIds.size(); i++) {
            if (fileKey <= aliveIds.get(i)) {
                nodeKeyIds.add(aliveIds.get(i));
                System.out.println("nodeKey 1" + nodeKeyIds.get(0));
                logger.logLine(Logger.INFO, "nodeKey 1 " + nodeKeyIds.get(0) + "for SDFSFileName" + sDFSFileName);

                if (aliveIds.size() >= 2) {
                    if (i + 1 > aliveIds.size() - 1) {
                        nodeKeyIds.add(aliveIds.get(i + 1 - aliveIds.size()));
                    } else {
                        nodeKeyIds.add(aliveIds.get(i + 1));
                    }
                    System.out.println("nodeKey 2" + nodeKeyIds.get(1));
                }
                if (aliveIds.size() >= 3) {
                    if (i + 2 > aliveIds.size() - 1) {
                        nodeKeyIds.add(aliveIds.get(i + 2 - aliveIds.size()));
                    } else {
                        nodeKeyIds.add(aliveIds.get(i + 2));
                    }
                    System.out.println("nodeKey 3" + nodeKeyIds.get(2));
                }
                break;
            }
        }

        if (nodeKeyIds.isEmpty() && aliveIds.size() >= 3) {
            nodeKeyIds.add(aliveIds.get(0));
            nodeKeyIds.add(aliveIds.get(1));
            nodeKeyIds.add(aliveIds.get(2));
        }


        List<Membership.Member> memberList = new ArrayList<>();
        if (!nodeKeyIds.isEmpty()) {
            Membership.Member member1 = Membership.Member.newBuilder().
                    setPort(Integer.parseInt(allMachinesId.get(nodeKeyIds.get(0)).split(":")[1]))
                    .setHost(allMachinesId.get(nodeKeyIds.get(0)).split(":")[0]).build();
            memberList.add(member1);
        }
        if (nodeKeyIds.size() >= 2) {
            Membership.Member member2 = Membership.Member.newBuilder().
                    setPort(Integer.parseInt(allMachinesId.get(nodeKeyIds.get(1)).split(":")[1]))
                    .setHost(allMachinesId.get(nodeKeyIds.get(1)).split(":")[0]).build();
            memberList.add(member2);

        }
        if (nodeKeyIds.size() >= 3) {
            Membership.Member member3 = Membership.Member.newBuilder().
                    setPort(Integer.parseInt(allMachinesId.get(nodeKeyIds.get(2)).split(":")[1]))
                    .setHost(allMachinesId.get(nodeKeyIds.get(2)).split(":")[0]).build();
            memberList.add(member3);
        }

        return memberList;
    }

    public int getLocalMachineNodeId() {

        int localNodeId = HashKey.hexToKey(HashKey.hashKey(getLocalHostName() + ":4444"));
        return localNodeId;
    }

    public List<Integer> getServerListNodeIdsForChunk(String sDFSFileName) {
        int fileKey = HashKey.hexToKey(HashKey.hashKey(sDFSFileName));
        List<Integer> aliveIds = new ArrayList<Integer>();

        for (Membership.Member member : this.membershipService.getMembershipList().getMemberList()) {
            aliveIds.add(HashKey.hexToKey(HashKey.hashKey(member.getHost() + ":" + member.getPort())));
        }

        List<Integer> nodeKeyIds = new ArrayList<Integer>();

        Collections.sort(aliveIds);
        for (int i = 0; i < aliveIds.size(); i++) {
            if (fileKey <= aliveIds.get(i)) {
                nodeKeyIds.add(aliveIds.get(i));
                System.out.println("nodeKey 1 " + nodeKeyIds.get(0));
                logger.logLine(Logger.INFO, "nodeKey 1 " + nodeKeyIds.get(0) + "for SDFSFileName" + sDFSFileName);

                if (aliveIds.size() >= 2) {
                    if (i + 1 > aliveIds.size() - 1) {
                        nodeKeyIds.add(aliveIds.get(i + 1 - aliveIds.size()));
                    } else {
                        nodeKeyIds.add(aliveIds.get(i + 1));
                    }
                    System.out.println("nodeKey 2 " + nodeKeyIds.get(1));
                }
                if (aliveIds.size() >= 3) {
                    if (i + 2 > aliveIds.size() - 1) {
                        nodeKeyIds.add(aliveIds.get(i + 2 - aliveIds.size()));
                    } else {
                        nodeKeyIds.add(aliveIds.get(i + 2));
                    }
                    System.out.println("nodeKey 3 " + nodeKeyIds.get(2));
                }
                break;
            }
        }

        if (nodeKeyIds.isEmpty() && aliveIds.size() >= 3) {
            nodeKeyIds.add(aliveIds.get(0));
            nodeKeyIds.add(aliveIds.get(1));
            nodeKeyIds.add(aliveIds.get(2));
        }

        return nodeKeyIds;
    }

    public List<Integer> getServerListNodeIdsForChunkNoLocal(String sDFSFileName) {
        int fileKey = HashKey.hexToKey(HashKey.hashKey(sDFSFileName));
        List<Integer> aliveIds = new ArrayList<Integer>();

        for (Membership.Member member : this.membershipService.getMembershipList().getMemberList()) {
            aliveIds.add(HashKey.hexToKey(HashKey.hashKey(member.getHost() + ":" + member.getPort())));
            System.out.println("alive members" + member.getHost() + aliveIds.toString());
        }

        List<Integer> nodeKeyIds = new ArrayList<Integer>();

        Collections.sort(aliveIds);
        for (int i = 0; i < aliveIds.size(); i++) {
            if (fileKey <= aliveIds.get(i)) {
                nodeKeyIds.add(aliveIds.get(i));
                System.out.println("nodeKey 1 " + nodeKeyIds.get(0));
                logger.logLine(Logger.INFO, "nodeKey 1 " + nodeKeyIds.get(0) + "for SDFSFileName" + sDFSFileName);

                if (aliveIds.size() >= 2) {
                    if (i + 1 > aliveIds.size() - 1) {
                        nodeKeyIds.add(aliveIds.get(i + 1 - aliveIds.size()));
                    } else {
                        nodeKeyIds.add(aliveIds.get(i + 1));
                    }
                    System.out.println("nodeKey 2 " + nodeKeyIds.get(1));
                }
                if (aliveIds.size() >= 3) {
                    if (i + 2 > aliveIds.size() - 1) {
                        nodeKeyIds.add(aliveIds.get(i + 2 - aliveIds.size()));
                    } else {
                        nodeKeyIds.add(aliveIds.get(i + 2));
                    }
                    System.out.println("nodeKey 3 " + nodeKeyIds.get(2));
                }
                break;
            }
        }

        if (nodeKeyIds.isEmpty() && aliveIds.size() >= 3) {
            nodeKeyIds.add(aliveIds.get(0));
            nodeKeyIds.add(aliveIds.get(1));
            nodeKeyIds.add(aliveIds.get(2));
        }

        System.out.println("NodeKeyId with localId " + nodeKeyIds.toString());

        int localNodeId = getLocalMachineNodeId();
        for (int i = 0; i < nodeKeyIds.size(); i++) {
            if (nodeKeyIds.get(i).equals(localNodeId)) nodeKeyIds.remove(i);
        }

        System.out.println("NodeKeyId without localId " + getLocalMachineNodeId() + nodeKeyIds.toString());
        return nodeKeyIds;
    }

    public String getMachineByNodeId(int nodeId) {
        return allMachinesId.get(nodeId);
    }


    public int getNodeIdOfMachine(Membership.Member member) {
        return HashKey.hexToKey(HashKey.hashKey(member.getHost() + ":" + member.getPort()));
    }
}
