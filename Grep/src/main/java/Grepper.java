import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by slekkala on 9/8/15.
 */
public class Grepper extends Thread {
    private static final Logger LOG = LoggerFactory.getLogger(Grepper.class);
    private Socket socket;


    public Grepper(Socket socket) {
        this.socket = socket;
        LOG.info("New conection from" + socket.getRemoteSocketAddress().toString());
    }

    public void run() {
        BufferedReader br = null;

        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        ) {
            String input = in.readLine();
            if (input == null || !input.startsWith("grep")) {
                return;
            }

            LOG.info("Grep command to be executed:" + input);
            br = Grepper.execute(input);

            String output;
            while ((output = br.readLine()) != null) {
                out.println(output);
            }

        } catch (IOException e) {
            LOG.info("Error handling client" + e);
        } finally {
            try {
                if (br != null) br.close();
                socket.close();
            } catch (IOException e) {
                LOG.info("Couldn't close a socket, what's going on?");
            }
            LOG.info("Connection with client closed");
        }
    }

    public static BufferedReader execute(String grepCommand) throws IOException {
        Runtime rt = Runtime.getRuntime();
        String[] commands = {"/bin/sh", "-c", grepCommand};
        Process proc = rt.exec(commands);

        return new BufferedReader(new InputStreamReader(proc.getInputStream()));
    }
}
