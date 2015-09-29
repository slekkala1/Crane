package tempest.commands.command;

import tempest.interfaces.UdpCommand;

public class Ping implements UdpCommand<Object, String> {
    private Object request;
    private String response;

    public String getCommandId() {
        return "grep";
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
