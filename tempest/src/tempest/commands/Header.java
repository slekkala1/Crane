package tempest.commands;

public class Header {
    private final String commandId;

    public Header(String commandId) {
        this.commandId = commandId;
    }

    public String getCommandId() {
        return commandId;
    }
}
