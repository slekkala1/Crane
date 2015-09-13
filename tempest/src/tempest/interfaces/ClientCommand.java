package tempest.interfaces;

public interface ClientCommand<TResponse extends CommandResponse<TResponse>> {
    String getRequest();
    TResponse getResponse(String response, int lineCount, long elapsedTime);
}
