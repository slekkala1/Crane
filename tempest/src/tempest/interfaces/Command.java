package tempest.interfaces;

public interface Command<TRequest, TResponse> {
    String getCommandId();
    TRequest getRequest();
    void setRequest(TRequest response);
    TResponse getResponse();
    void setResponse(TResponse response);
    TResponse add(TResponse response1, TResponse response2);
}
