package tempest.xcommands.server;

import tempest.interfaces.ServerCommand;
import tempest.services.Logger;

public class Grep implements ServerCommand {
    private final Logger logger;

    public Grep(Logger logger) {

        this.logger = logger;
    }

    public boolean canExecute(String message) {
        return message.startsWith("grep ");
    }

    public String execute(String message) {
        return logger.grep(message.substring(5));
    }
}
