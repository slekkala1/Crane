import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * Created by slekkala on 9/8/15.
 */
public class GrepServer {

    private static final Logger LOG = Logger.getLogger(GrepServer.class);


    public static void main(String[] args) throws Exception {
        BasicConfigurator.configure();
        LOG.info("The Grep server is running.");
        ServerSocket listener = new ServerSocket(9898);
        try {
            while (true) {
                new Grepper(listener.accept()).start();
            }
        } finally {
            listener.close();
        }
    }
}
