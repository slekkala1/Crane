import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.ServerSocket;

/**
 * Created by slekkala on 9/8/15.
 */
public class GrepServer {
    private static final Logger LOG = LoggerFactory.getLogger(GrepServer.class);

    public static void main(String[] args) throws Exception {
        BasicConfigurator.configure();
        LOG.info("The Grep server is running.");
        try (ServerSocket listener = new ServerSocket(9898)) {
            while (true) {
                new Grepper(listener.accept()).start();
            }
        }
    }
}
