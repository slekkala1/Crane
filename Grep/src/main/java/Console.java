import asg.cliche.Command;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Created by slekkala on 8/30/15.
 */
public class Console {

    private final GrepClient grepClient;

    public Console(GrepClient grepClient) {
        this.grepClient = grepClient;
    }

    @Command
    public void grep(String grepCommand) throws IOException, InterruptedException, ExecutionException {
        grepClient.grepAllMachines(grepCommand);
    }

}
