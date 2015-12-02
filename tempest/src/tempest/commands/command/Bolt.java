package tempest.commands.command;

import tempest.commands.interfaces.ResponseCommand;
import tempest.commands.interfaces.Tcp;
import tempest.protos.Command;
import tempest.services.Tuple;

import java.util.*;
import java.util.List;

/**
 * Created by swapnalekkala on 11/24/15.
 */
public class Bolt implements ResponseCommand<String, String>, Tcp {
    public static final tempest.protos.Command.Message.Type type = Command.Message.Type.BOLT;
    private String request;
    private String response;
    private List<Tuple> tuplesList;

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

    public String add(String response1, String response2) {
        return response1 + System.lineSeparator() + response2;
    }

    public void setTuplesList(List<Tuple> tuplesList) {
        this.tuplesList = tuplesList;
    }

    public List<Tuple> getTuplesList() {
        return tuplesList;
    }
}