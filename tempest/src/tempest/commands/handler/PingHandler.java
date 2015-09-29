package tempest.commands.handler;

import tempest.commands.command.Ping;
import tempest.interfaces.CommandHandler;

public class PingHandler implements CommandHandler<Ping, Object, String> {
    public String getCommandId() {
        return "ping";
    }

    public boolean canHandle(String commandId) {
        return getCommandId().equals(commandId);
    }

    public String serialize(Ping command) {
        return System.lineSeparator() + command.getResponse();
    }

    public Ping deserialize(String request, String response) {
        Ping ping = new Ping();
        ping.setResponse(response);
        return ping;
    }

    public String execute(Object request) {
        return "Hello";
    }
}
