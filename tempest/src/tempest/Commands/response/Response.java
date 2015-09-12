package tempest.commands.response;

import tempest.interfaces.CommandResponse;

public class Response implements CommandResponse<Response> {
    public final String response;
    public final int lineCount;

    public Response(String response, int lineCount) {
        this.response = response;
        this.lineCount = lineCount;
    }

    public String getResponse() {
        return response;
    }

    public int getLineCount() {
        return lineCount;
    }

    public Response add(Response response) {
        return new Response(getResponse() + response.getResponse(), getLineCount() + response.getLineCount());
    }
}
