package tempest.interfaces;

import tempest.commands.Response;

import java.util.concurrent.Callable;

public interface ClientResponseCommandExecutor<TResponse> extends Callable<Response<TResponse>> {
    Response<TResponse> execute();
}
