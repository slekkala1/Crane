package tempest.commands.client;

import tempest.commands.response.Response;
import tempest.interfaces.ClientCommand;

public class Grep implements ClientCommand<Response> {
    private final String options;

    public Grep(String options) {
        this.options = options;
    }

    public String getRequest() {
        return "grep " + options;
    }

    public Response getResponse(String response, int lineCount) {
        return new Response(response, lineCount);
    }
}
