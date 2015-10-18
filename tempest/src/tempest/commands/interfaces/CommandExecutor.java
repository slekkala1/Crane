package tempest.commands.interfaces;

public interface CommandExecutor<TCommand extends Command<TRequest>, TRequest> extends CommandHandler<TCommand> {
    void execute(TRequest request);
}
