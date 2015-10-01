package tempest.commands.command;

import tempest.interfaces.Command;

public class Grep implements Command<String, String> {
    public static final String id = "grep";
    private String request;
    private String response;

    public String getCommandId() {
        return id;
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
