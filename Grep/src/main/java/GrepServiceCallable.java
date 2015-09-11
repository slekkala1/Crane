import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;

/**
 * Created by slekkala on 8/31/15.
 */
public class GrepServiceCallable implements Callable<String> {

    private static final Logger LOG = LoggerFactory.getLogger(GrepServiceCallable.class);


    private String grepCommand;
    private String hostName;

    public GrepServiceCallable(String grepCommand, String hostName) {
        this.grepCommand = grepCommand;
        this.hostName = hostName;
    }

    @Override
    public String call() {
        return grepUsingSocket(grepCommand);
    }

    public String grepUsingSocket(String grepCommand) {
        int port = 9898;

        try {
            LOG.info("Connecting to " + this.hostName + " on port " + port);
            Socket client = new Socket(hostName, port);
            LOG.info("Just connected to " + hostName);

            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            PrintWriter out = new PrintWriter(client.getOutputStream(), true);

            out.println(grepCommand);

            String output;
            while ((output = in.readLine()) != null) {
                LOG.info("[" + hostName + "]>" + output + System.getProperty("line.separator"));
            }

            client.close();
            LOG.info("[" + hostName + " FINISHED]");
        } catch (UnknownHostException e) {
            LOG.error("Unknown host exception while connecting to " + this.hostName + e.toString());
        } catch (IOException e) {
            LOG.error("IOException " + e.toString() + " to " + hostName);
        }

        return new StringBuilder().toString();
    }
}
