package tempest.interfaces;

public interface Command<TRequest, TResponse> {
    tempest.protos.Command.Message.Type getType();
    TRequest getRequest();
    void setRequest(TRequest response);
    TResponse getResponse();
    void setResponse(TResponse response);
    TResponse add(TResponse response1, TResponse response2);
}
