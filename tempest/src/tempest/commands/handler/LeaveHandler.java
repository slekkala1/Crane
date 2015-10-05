package tempest.commands.handler;

import tempest.MembershipService;
import tempest.commands.command.Leave;
import tempest.interfaces.CommandHandler;
import tempest.protos.Command;
import tempest.protos.Membership;

public class LeaveHandler implements CommandHandler<Leave, Membership.Member, String> {
    private final MembershipService membershipService;

    public LeaveHandler(MembershipService membershipService) {
        this.membershipService = membershipService;
    }

    public boolean canHandle(Command.Message.Type type) {
        return Leave.type == type;
    }

    public Command.Message serialize(Leave command) {
        Command.Leave.Builder leaveBuilder = Command.Leave.newBuilder().setRequest(command.getRequest());
        if (command.getResponse() != null)
            leaveBuilder.setResponse(command.getResponse());
        Command.Message message = Command.Message.newBuilder()
                .setType(Command.Message.Type.LEAVE)
                .setLeave(leaveBuilder)
                .build();
        return message;
    }

    public Leave deserialize(Command.Message message) {
        Leave leave = new Leave();
        leave.setRequest(message.getIntroduce().getRequest());
        if (message.getIntroduce().hasResponse())
            leave.setResponse(message.getLeave().getResponse());
        return leave;
    }

    public String execute(Membership.Member request) {
        membershipService.memberLeft(request);
        return "Bye";
    }
}
