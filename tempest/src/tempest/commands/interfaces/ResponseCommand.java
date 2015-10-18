package tempest.commands.interfaces;

public interface ResponseCommand<TRequest, TResponse> extends Command<TRequest> {
    TResponse getResponse();
    void setResponse(TResponse response);
    TResponse add(TResponse response1, TResponse response2);
}
