package tempest.commands.handler;

import com.google.protobuf.InvalidProtocolBufferException;
import tempest.MembershipService;
import tempest.commands.command.Introduce;
import tempest.interfaces.CommandHandler;
import tempest.interfaces.Logger;
import tempest.protos.Membership;

public class IntroduceHandler implements CommandHandler<Introduce, Membership.Member, Membership.MembershipList> {
    private final MembershipService membershipService;
    private final Logger logger;

    public IntroduceHandler(MembershipService membershipService, Logger logger) {
        this.membershipService = membershipService;
        this.logger = logger;
    }

    public String getCommandId() {
        return Introduce.id;
    }

    public boolean canHandle(String commandId) {
        return getCommandId().equals(commandId);
    }

    public String serialize(Introduce command) {
        return command.getRequest() + System.lineSeparator()
                + command.getResponse();
    }

    public Introduce deserialize(String request, String response) {
        Introduce introduce = new Introduce();
        try {
            introduce.setRequest(Membership.Member.parseFrom(request.getBytes()));
            introduce.setResponse(Membership.MembershipList.parseFrom(response.getBytes()));
            return introduce;

        } catch (InvalidProtocolBufferException e) {
            logger.logLine(Logger.SEVERE, "Protobuf failed to deserialize Introduce");
        }
        return null;
    }

    public Membership.MembershipList execute(Membership.Member request) {
        membershipService.addMember(request);
        return membershipService.getMembershipList();
    }
}
