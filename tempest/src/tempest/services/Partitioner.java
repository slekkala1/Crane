package tempest.services;

import org.apache.commons.lang3.tuple.ImmutablePair;
import tempest.protos.Membership;

import java.util.*;

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


    private final MembershipService membershipService;
    private final Map<Integer, String> allMachinesId = new HashMap<Integer, String>();

    private Map<ImmutablePair<String, Integer>, Chunk> metadata = new HashMap<ImmutablePair<String, Integer>, Chunk>();

    public Partitioner(MembershipService membershipService) {
        this.membershipService = membershipService;
        getAllServerNodeIds();
    }

    public List<Membership.Member> getAllServersThatAreAlive() {
        return this.membershipService.getMembershipList().getMemberList();
    }

    public void getAllServerNodeIds() {
        for(int i =0; i <allMachines.size();i++) {
            allMachinesId.put(HashKey.hexToKey(HashKey.hashKey(allMachines.get(i))),
                    allMachines.get(i));
        }
    }


    public Membership.Member getServerToSendChunkTo(String sDFSFileName) {
        int fileKey = HashKey.hexToKey(HashKey.hashKey(sDFSFileName));
        int nodeKey=0;
        List<Integer> aliveIds = new ArrayList<Integer>();
        //System.out.println("aliveIds" + Arrays.toString(aliveIds));
        for (Membership.Member member : this.membershipService.getMembershipList().getMemberList()) {
            aliveIds.add(HashKey.hexToKey(HashKey.hashKey(member.getHost() + ":" + member.getPort())));
        }

        Collections.sort(aliveIds);
        for (Integer i : aliveIds ) {
            if (fileKey == i) nodeKey = i;
            if(fileKey <i) {
                nodeKey = i;
                break;
            }
        }
        System.out.println("aliveIds" + nodeKey);
        System.out.println(allMachinesId.get(nodeKey));

        return  Membership.Member.newBuilder().
                setPort(Integer.parseInt(allMachinesId.get(nodeKey).split(":")[1]))
                .setHost(allMachinesId.get(nodeKey).split(":")[0]).build();
    }




    public void updateFileMetadata(String sDFSFileName, String chunkName, int chunkID, int chunkSize, Membership.Member member,
                                   int numberOfChunks, int totalSize) {
        if (metadata.containsKey(new ImmutablePair<String, Integer>(sDFSFileName, chunkID))) {
            metadata.get(new ImmutablePair<String, Integer>(sDFSFileName, chunkID)).addMember(member);
        } else {
            Chunk chunk = new Chunk(chunkID, chunkName, sDFSFileName, chunkSize, numberOfChunks, totalSize);
            chunk.addMember(member);
            metadata.put(new ImmutablePair(sDFSFileName, chunkID), chunk);
        }
//        System.out.println(metadata.);
    }

    public List<Membership.Member> getServerListByChunkId(String sDFSFileName, int chunkID) {
        return this.metadata.get(new ImmutablePair(sDFSFileName, chunkID)).getServerList();
    }

    public String getChunkNameByChunkId(String sDFSFileName, int chunkID) {
        return this.metadata.get(new ImmutablePair(sDFSFileName, chunkID)).getChunkName();
    }

    public int getChunkSizeByChunkId(String sDFSFileName, int chunkID) {
        return this.metadata.get(new ImmutablePair(sDFSFileName, chunkID)).getChunkSize();
    }

    public int getNumberOfChunks(String sDFSFileName) {
        return this.metadata.get(new ImmutablePair(sDFSFileName, 0)).getNumberOfChunks();
    }

    public int getTotalFilesize(String sDFSFileName) {
        return this.metadata.get(new ImmutablePair(sDFSFileName, 0)).getTotalSize();
    }


}
