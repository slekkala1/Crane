package tempest.commands.command;

import tempest.interfaces.UdpCommand;
import tempest.protos.Command;

public class Ping implements UdpCommand<Object, String> {
    public static final Command.Message.Type type = Command.Message.Type.PING;
    private Object request;
    private String response;

    public Command.Message.Type getType() {
        return type;
    }

    public Object getRequest() {
        return request;
    }

    public void setRequest(Object request) {
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
