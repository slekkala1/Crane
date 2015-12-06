package tempest.commands.command;

import tempest.commands.interfaces.ResponseCommand;
import tempest.commands.interfaces.Tcp;
import tempest.protos.Command;

/**
 * Created by swapnalekkala on 12/5/15.
 */
public class Ack implements ResponseCommand<String, String>, Tcp {
    public static final tempest.protos.Command.Message.Type type = Command.Message.Type.ACK;
    private int id;
    private String request;
    private String response;

    public tempest.protos.Command.Message.Type getType() {
        return type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
