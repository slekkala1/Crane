package tempest.commands.handler;

import tempest.MembershipService;
import tempest.commands.command.Introduce;
import tempest.interfaces.CommandHandler;
import tempest.interfaces.Logger;
import tempest.protos.Command;
import tempest.protos.Membership;

public class IntroduceHandler implements CommandHandler<Introduce, Membership.Member, Membership.MembershipList> {
    private final MembershipService membershipService;
    private final Logger logger;

    public IntroduceHandler(MembershipService membershipService, Logger logger) {
        this.membershipService = membershipService;
        this.logger = logger;
    }

    public boolean canHandle(Command.Message.Type type) {
        return Introduce.type == type;
    }

    public Command.Message serialize(Introduce command) {
        Command.Message message = Command.Message.newBuilder()
                .setType(Command.Message.Type.INTRODUCE)
                .setIntroduce(Command.Introduce.newBuilder()
                        .setRequest(command.getRequest())
                        .setResponse(command.getResponse()).build())
                .build();
        return message;
    }

    public Introduce deserialize(Command.Message message) {
        Introduce introduce = new Introduce();
        introduce.setRequest(message.getIntroduce().getRequest());
        if (message.getIntroduce().hasResponse())
            introduce.setResponse(message.getIntroduce().getResponse());
        return introduce;
    }

    public Membership.MembershipList execute(Membership.Member request) {
        membershipService.addMember(request);
        return membershipService.getMembershipList();
    }
}
