package tempest.interfaces;

public interface CommandHandler<TCommand extends Command<TRequest, TResponse>, TRequest, TResponse> {
    String getCommandId();
    boolean canHandle(String commandId);
    String serialize(TCommand command);
    TCommand deserialize(String request, String response);
    TResponse execute(TRequest request);
}
