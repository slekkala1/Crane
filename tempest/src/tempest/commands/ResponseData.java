package tempest.commands;

public class ResponseData {
    public final int lineCount;
    public final long queryLatency;

    public ResponseData(int lineCount, long queryLatency) {
        this.lineCount = lineCount;
        this.queryLatency = queryLatency;
    }

    public int getLineCount() {
        return lineCount;
    }

    public long getQueryLatency() {
        return queryLatency;
    }

    public ResponseData add(ResponseData response) {
        return new ResponseData(getLineCount() + response.getLineCount(),
                Math.max(getQueryLatency(), response.getQueryLatency())); //Max rather than the sum for parallel
    }
}
