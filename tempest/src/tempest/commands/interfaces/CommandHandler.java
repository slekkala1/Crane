package tempest.commands.interfaces;

public interface CommandHandler<TCommand extends Command> {
    boolean canHandle(tempest.protos.Command.Message.Type type);
    tempest.protos.Command.Message serialize(TCommand command);
    TCommand deserialize(tempest.protos.Command.Message message);
}

