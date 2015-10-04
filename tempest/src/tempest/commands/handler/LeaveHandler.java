package tempest.commands.handler;

import com.google.protobuf.InvalidProtocolBufferException;
import tempest.MembershipService;
import tempest.commands.command.Introduce;
import tempest.commands.command.Leave;
import tempest.interfaces.CommandHandler;
import tempest.interfaces.Logger;
import tempest.protos.Membership;

public class LeaveHandler implements CommandHandler<Leave, Membership.Member, String> {
    private final MembershipService membershipService;
    private final Logger logger;

    public LeaveHandler(MembershipService membershipService, Logger logger) {
        this.membershipService = membershipService;
        this.logger = logger;
    }

    public String getCommandId() {
        return Introduce.id;
    }

    public boolean canHandle(String commandId) {
        return getCommandId().equals(commandId);
    }

    public String serialize(Leave command) {
        return command.getRequest() + System.lineSeparator()
                + command.getResponse();
    }

    public Leave deserialize(String request, String response) {
        Leave leave = new Leave();
        try {
            leave.setRequest(Membership.Member.parseFrom(request.getBytes()));
            leave.setResponse(response);
            return leave;

        } catch (InvalidProtocolBufferException e) {
            logger.logLine(Logger.SEVERE, "Protobuf failed to deserialize Leave");
        }
        return null;
    }

    public String execute(Membership.Member request) {
        membershipService.memberLeft(request);
        return "Bye";
    }
}
