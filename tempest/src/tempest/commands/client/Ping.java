package tempest.commands.client;

import tempest.commands.response.Response;
import tempest.interfaces.UdpClientCommand;

public class Ping implements UdpClientCommand<Response> {
    public String getRequest() {
        return "ping";
    }

    public Response getResponse(String response, int lineCount, long elapsedTime) {
        return new Response(response, lineCount, elapsedTime);
    }
}
