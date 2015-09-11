import asg.cliche.ShellFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;

/**
 * Created by slekkala on 9/9/15.
 */
public class ClientApp {
    private static final Logger LOG = LoggerFactory.getLogger(ClientApp.class);

    public static void main(String[] args) throws IOException {

        ReadPropertiesFile propertiesFile = new ReadPropertiesFile();
        String[] machines = propertiesFile.readPropertiesFile().split(",");
        LOG.info("List of machines " + Arrays.toString(machines));

        Console console = new Console(new GrepClient(machines));
        ShellFactory.createConsoleShell("CS425-MP-Lekkala-Morrow", "", console).commandLoop();
    }
}
