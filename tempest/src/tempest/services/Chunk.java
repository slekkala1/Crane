package tempest.services;

import tempest.protos.Membership;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by swapnalekkala on 10/30/15.
 */
public class Chunk {

    private final int chunkId;
    private final String chunkName;
    private final String fileName;
    private final int chunkSize;
    private List<Membership.Member> serverList;
    private int numberOfChunks;
    private int totalSize;

    public Chunk(int chunkId, String chunkName, String fileName, int chunkSize, int numberOfChunks, int totalSize) {
        this.chunkId = chunkId;
        this.chunkName = chunkName;
        this.fileName = fileName;
        this.chunkSize = chunkSize;
        this.serverList = new ArrayList<>();
        this.numberOfChunks = numberOfChunks;
        this.totalSize = totalSize;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public int getChunkId() {
        return chunkId;
    }

    public void addMember(Membership.Member member) {
        this.serverList.add(member);
    }

    public String getChunkName() {
        return chunkName;
    }

    public int getNumberOfChunks() {
        return numberOfChunks;
    }

    public String getFileName() {
        return fileName;
    }

    public List<Membership.Member> getServerList() {
        return serverList;
    }

    public int getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(int totalSize) {
        this.totalSize = totalSize;
    }
}
