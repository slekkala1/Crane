package tempest.interfaces;

import java.util.concurrent.Callable;

public interface ClientCommandExecutor<TResponse extends CommandResponse<TResponse>> extends Callable<TResponse> {
    TResponse execute();
}
