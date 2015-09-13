package tempest.xcommands.response;

import tempest.interfaces.CommandResponse;

public class Response implements CommandResponse<Response> {
    public final String response;
    public final int lineCount;
    public final long queryLatency;

    public Response(String response, int lineCount, long queryLatency) {
        this.response = response;
        this.lineCount = lineCount;
        this.queryLatency = queryLatency;
    }

    public String getResponse() {
        return response;
    }

    public int getLineCount() {
        return lineCount;
    }

    public long getQueryLatency() {
        return queryLatency;
    }

    public Response add(Response response) {
        return new Response(getResponse() + response.getResponse(),
                getLineCount() + response.getLineCount(),
                Math.max(getQueryLatency(), response.getQueryLatency())); //Max rather than the sum for parallel
    }
}
