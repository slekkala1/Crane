import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.apache.log4j.Logger;

/**
 * Created by slekkala on 9/8/15.
 */
public class Grepper extends Thread {
    private static final Logger LOG = Logger.getLogger(Grepper.class);
    private Socket socket;


    public Grepper(Socket socket) {
        this.socket = socket;
        LOG.info("New conection from");
    }

    public void run() {
        try {

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            String input = in.readLine();
            if (input == null || !input.startsWith("grep")) {
                return;
            }

            String output;
            LOG.info(input);
            BufferedReader br = Grepper.grepfunctionality(input);
            while ((output = br.readLine()) != null) {
                out.println(output);
            }

        } catch (IOException e) {
            LOG.info("Error handling client" + e);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                LOG.info("Couldn't close a socket, what's going on?");
            }
            LOG.info("Connection with client closed");
        }
    }

    public static BufferedReader grepfunctionality(String grepCommand) throws IOException {
        Runtime rt = Runtime.getRuntime();
        String[] commands = {"/bin/sh", "-c", grepCommand};
        Process proc = rt.exec(commands);

        System.out.println(commands[2]);
        BufferedReader reader = new BufferedReader(new
                InputStreamReader(proc.getInputStream()));

        return reader;

    }
}
