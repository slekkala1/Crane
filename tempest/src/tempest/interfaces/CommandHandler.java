package tempest.interfaces;

public interface CommandHandler<TCommand extends Command<TRequest, TResponse>, TRequest, TResponse> {
    boolean canHandle(tempest.protos.Command.Message.Type type);
    tempest.protos.Command.Message serialize(TCommand command);
    TCommand deserialize(tempest.protos.Command.Message message);
    TResponse execute(TRequest request);
}
