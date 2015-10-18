package tempest.commands.interfaces;

public interface Command<TRequest> {
    tempest.protos.Command.Message.Type getType();
    TRequest getRequest();
    void setRequest(TRequest response);
}
