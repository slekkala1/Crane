package tempest.interfaces;

import java.util.concurrent.Callable;

public interface ClientCommandExecutor extends Callable {
    void execute();
}
