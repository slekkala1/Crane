package tempest.Commands;

import tempest.Command;

public class Ping extends Command {
    @Override
    public boolean canExecute(String message) {
        return message.startsWith("ping");
    }

    @Override
    public String execute(String message) {
        return "Hello";
    }
}
