package tempest.commands.client;

import tempest.commands.response.Response;
import tempest.interfaces.ClientCommand;

public class Ping implements ClientCommand<Response> {
    public String getRequest() {
        return "ping";
    }

    public Response getResponse(String response, int lineCount, long elapsedTime) {
        return new Response(response + System.getProperty("line.separator"), lineCount, elapsedTime);
    }
}
