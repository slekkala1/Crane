package tempest.commands.command;

import tempest.commands.interfaces.ResponseCommand;
import tempest.commands.interfaces.Tcp;
import tempest.protos.Command;

import java.util.List;
/**
 * Created by swapnalekkala on 12/5/15.
 */
public class Topology implements ResponseCommand<String, String>, Tcp {
    public static final tempest.protos.Command.Message.Type type = tempest.protos.Command.Message.Type.TOPOLOGY;
    private Spout spout;
    private List<Bolt> boltList;
    private String response;
    private String request;

    public tempest.protos.Command.Message.Type getType() {
        return type;
    }

    public Spout getSpout() {
        return spout;
    }

    public void setSpout(Spout spout) {
        this.spout = spout;
    }

    public List<Bolt> getBoltList() {
        return boltList;
    }

    public void setBoltList(List<Bolt> boltList) {
        this.boltList = boltList;
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

    public String add(String response1, String response2) {
        return response1 + System.lineSeparator() + response2;
    }
}
