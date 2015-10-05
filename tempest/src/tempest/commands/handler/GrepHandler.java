package tempest.commands.handler;

import tempest.commands.command.Grep;
import tempest.interfaces.CommandHandler;
import tempest.interfaces.Logger;
import tempest.protos.Command;

public class GrepHandler implements CommandHandler<Grep, String, String> {
    private final Logger logger;

    public GrepHandler(Logger logger) {

        this.logger = logger;
    }

    public boolean canHandle(Command.Message.Type type) {
        return Grep.type == type;
    }

    public Command.Message serialize(Grep command) {
        Command.Message message = Command.Message.newBuilder()
                .setType(Command.Message.Type.GREP)
                .setGrep(Command.Grep.newBuilder()
                        .setRequest(command.getRequest())
                        .setResponse(command.getResponse()))
                .build();
        return message;
    }

    public Grep deserialize(Command.Message message) {
        Grep grep = new Grep();
        grep.setRequest(message.getGrep().getRequest());
        if (message.getGrep().hasResponse())
        grep.setResponse(message.getGrep().getResponse());
        return grep;
    }

    public String execute(String request) {
        return logger.grep(request);
    }
}
