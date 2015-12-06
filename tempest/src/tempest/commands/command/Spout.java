package tempest.commands.command;

import tempest.commands.interfaces.ResponseCommand;
import tempest.commands.interfaces.Tcp;
import tempest.protos.Command;
import tempest.services.Tuple;
import tempest.protos.Membership;


import java.util.List;

/**
 * Created by swapnalekkala on 11/24/15.
 */
public class Spout implements ResponseCommand<String, String>, Tcp {
    public static final tempest.protos.Command.Message.Type type = tempest.protos.Command.Message.Type.SPOUT;
    private int id;
    private String request;
    private String response;
    private List<Tuple> tuplesList;
    public tempest.protos.Command.Spout.SpoutType spoutType;
    private List<Membership.Member> sendTo;


    public List<Membership.Member> getSendTo() {
        return sendTo;
    }

    public void setSendTo(List<Membership.Member> sendTo) {
        this.sendTo = sendTo;
    }

    public Command.Spout.SpoutType getSpoutType() {
        return spoutType;
    }

    public void setSpoutType(Command.Spout.SpoutType spoutType) {
        this.spoutType = spoutType;
    }

    public tempest.protos.Command.Message.Type getType() {
        return type;
    }

    public String getRequest() {
        return request;
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

    public List<Tuple> getTuplesList() {
        return tuplesList;
    }

    public void setTuplesList(List<Tuple> tuplesList) {
        this.tuplesList = tuplesList;
    }

    public String add(String response1, String response2) {
        return response1 + System.lineSeparator() + response2;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}