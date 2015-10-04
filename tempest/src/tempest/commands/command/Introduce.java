package tempest.commands.command;

import tempest.interfaces.Command;
import tempest.protos.Membership;
import tempest.services.MembershipListUtil;

public class Introduce implements Command<Membership.Member, Membership.MembershipList> {
    public static final String id = "introduce";
    private Membership.Member request;
    private Membership.MembershipList response;

    public String getCommandId() {
        return id;
    }

    public Membership.Member getRequest() {
        return request;
    }

    public void setRequest(Membership.Member request) {
        this.request = request;
    }

    public Membership.MembershipList getResponse() {
        return response;
    }

    public void setResponse(Membership.MembershipList response) {
        this.response = response;
    }

    public Membership.MembershipList add(Membership.MembershipList response1, Membership.MembershipList response2) {
        return MembershipListUtil.mergeMembershipList(response1, response2);
    }
}
