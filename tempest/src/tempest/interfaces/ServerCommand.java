package tempest.interfaces;

public interface ServerCommand {
    boolean canExecute(String message);
    String execute(String message);
}
