package tempest.commands.server;

import tempest.interfaces.Logger;
import tempest.interfaces.ServerCommand;
import tempest.services.DefaultLogger;

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
