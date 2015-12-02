package tempest.commands.command;

import tempest.commands.interfaces.ResponseCommand;
import tempest.commands.interfaces.Tcp;
import tempest.services.Tuple;
import java.util.List;

/**
 * Created by swapnalekkala on 11/24/15.
 */
public class Spout implements ResponseCommand<String, String>, Tcp {
    public static final tempest.protos.Command.Message.Type type = tempest.protos.Command.Message.Type.SPOUT;
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

    public List<Tuple> getTuplesList() {
        return tuplesList;
    }

    public void setTuplesList(List<Tuple> tuplesList) {
        this.tuplesList = tuplesList;
    }

    public String add(String response1, String response2) {
        return response1 + System.lineSeparator() + response2;
    }
}