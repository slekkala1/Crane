import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.Callable;

/**
 * Created by slekkala on 8/31/15.
 */
public class GrepServiceCallable implements Callable<String> {

    private static final Logger LOG = Logger.getLogger(GrepServiceCallable.class);


    private String grepCommand;

    public GrepServiceCallable(String grepCommand) {
        this.grepCommand = grepCommand;
    }

    @Override
    public String call() {
        return grepUsingSocket(grepCommand);
    }

    public String grepUsingSocket(String grepCommand) {
        String serverAddress = "127.0.0.1";
        int port = 9898;
        BasicConfigurator.configure();
        StringBuilder resultBuilder = new StringBuilder();

        try
        {
            LOG.info("Connecting to " + serverAddress +
                    " on port " + port);
            Socket client = new Socket(serverAddress, port);
            System.out.println("Just connected to "
                    + client.getRemoteSocketAddress());
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(client.getInputStream()));
            PrintWriter out = new PrintWriter(client.getOutputStream(), true);


            out.println(grepCommand);

            String output;

            while((output=in.readLine())!=null) {
                resultBuilder.append(client.getRemoteSocketAddress()+ "-" + output).append(System.getProperty("line.separator"));
            }
            client.close();
        }catch(IOException e)
        {
            e.printStackTrace();
        }
        return resultBuilder.toString();
    }


}
