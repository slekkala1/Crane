package tempest.commands.interfaces;

public interface ResponseCommandExecutor<TCommand extends ResponseCommand<TRequest, TResponse>, TRequest, TResponse> extends CommandHandler<TCommand> {
    TResponse execute(TRequest request);
}
