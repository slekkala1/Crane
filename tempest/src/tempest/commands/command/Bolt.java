package tempest.commands.command;

import tempest.commands.interfaces.ResponseCommand;
import tempest.commands.interfaces.Tcp;
import tempest.protos.*;
import tempest.protos.Membership;
import tempest.services.Tuple;

import java.util.*;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by swapnalekkala on 11/24/15.
 */
public class Bolt implements ResponseCommand<String, String>, Tcp {
    public static final tempest.protos.Command.Message.Type type = Command.Message.Type.BOLT;
    private String request;
    private String response;
    private Membership.Member receiveFrom;
    private int receiveFromID;
    private List<Tuple> tuplesList;
    private LinkedBlockingQueue<Tuple> tuplesQueue;
    private Membership.Member sendTupleTo;
    private int sendTupleToID;
    private int id;
    private tempest.protos.Command.Bolt.BoltType boltType;
    private int parallelism;

    public int getParallelism() {
        return parallelism;
    }

    public void setParallelism(int parallelism) {
        this.parallelism = parallelism;
    }

    public int getSendTupleToID() {
        return sendTupleToID;
    }

    public void setSendTupleToID(int sendTupleToID) {
        this.sendTupleToID = sendTupleToID;
    }

    public int getReceiveFromID() {
        return receiveFromID;
    }

    public void setReceiveFromID(int receiveFromID) {
        this.receiveFromID = receiveFromID;
    }

    public Membership.Member getReceiveFrom() {
        return receiveFrom;
    }

    public void setReceiveFrom(Membership.Member receiveFrom) {
        this.receiveFrom = receiveFrom;
    }

    public Command.Bolt.BoltType getBoltType() {
        return boltType;
    }

    public void setBoltType(Command.Bolt.BoltType boltType) {
        this.boltType = boltType;
    }

    public tempest.protos.Command.Message.Type getType() {
        return type;
    }

    public String getRequest() {
        return request;
    }

    public Membership.Member getSendTupleTo() {
        return sendTupleTo;
    }

    public void setSendTupleTo(Membership.Member sendTupleTo) {
        this.sendTupleTo = sendTupleTo;
    }


    public void setRequest(String request) {
        this.request = request;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String add(String response1, String response2) {
        return response1 + System.lineSeparator() + response2;
    }

    public void setTuplesList(List<Tuple> tuplesList) {
        this.tuplesList = tuplesList;
    }

    public List<Tuple> getTuplesList() {
        return tuplesList;
    }

    public LinkedBlockingQueue<Tuple> getTuplesQueue() {
        return tuplesQueue;
    }

    public void setTuplesQueue(LinkedBlockingQueue<Tuple> tuplesQueue) {
        this.tuplesQueue = tuplesQueue;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}