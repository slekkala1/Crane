package tempest.commands.handler;

import tempest.commands.command.Grep;
import tempest.interfaces.CommandHandler;
import tempest.interfaces.Logger;

public class GrepHandler implements CommandHandler<Grep, String, String> {
    private final Logger logger;

    public GrepHandler(Logger logger) {

        this.logger = logger;
    }

    public String getCommandId() {
        return Grep.id;
    }

    public boolean canHandle(String commandId) {
        return getCommandId().equals(commandId);
    }

    public String serialize(Grep command) {
        return command.getRequest() + System.lineSeparator() + command.getResponse();
    }

    public Grep deserialize(String request, String response) {
        Grep grep = new Grep();
        grep.setRequest(request);
        grep.setResponse(response);
        return grep;
    }

    public String execute(String request) {
        return logger.grep(request);
    }
}
