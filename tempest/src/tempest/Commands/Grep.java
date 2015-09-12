package tempest.Commands;

import tempest.Command;
import tempest.services.Logger;

public class Grep extends Command {
    private final Logger logger;

    public Grep(Logger logger) {

        this.logger = logger;
    }

    @Override
    public boolean canExecute(String message) {
        return message.startsWith("grep ");
    }

    @Override
    public String execute(String message) {
        return logger.grep(message.substring(5));
    }
}
