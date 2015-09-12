package tempest;

public abstract class Command {
    public abstract boolean canExecute(String message);
    public abstract String execute(String message);
}
