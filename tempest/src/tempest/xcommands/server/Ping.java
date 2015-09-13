package tempest.xcommands.server;

import tempest.interfaces.ServerCommand;

public class Ping implements ServerCommand {
    public boolean canExecute(String message) {
        return message.startsWith("ping");
    }

    public String execute(String message) {
        return "Hello";
    }
}
