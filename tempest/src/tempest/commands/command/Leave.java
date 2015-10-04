package tempest.commands.command;

import tempest.interfaces.Command;
import tempest.protos.Membership;
import tempest.services.MembershipListUtil;

public class Leave implements Command<Membership.Member, String> {
    public static final String id = "leave";
    private Membership.Member request;
    private String response;

    public String getCommandId() {
        return id;
    }

    public Membership.Member getRequest() {
        return request;
    }

    public void setRequest(Membership.Member request) {
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
