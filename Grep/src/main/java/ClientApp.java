import asg.cliche.ShellFactory;
import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;

/**
 * Created by slekkala on 9/9/15.
 */
public class ClientApp {
    private static final Logger LOG = LoggerFactory.getLogger(ClientApp.class);

    public static void main(String[] args) throws IOException {
        BasicConfigurator.configure();

        ReadPropertiesFile propertiesFile = new ReadPropertiesFile();
        String[] machines = propertiesFile.readPropertiesFile().split(",");
        LOG.info("List of machines " + Arrays.toString(machines));

        Console console = new Console(new GrepClient(machines));
        ShellFactory.createConsoleShell("CS425-MP-Lekkala-Morrow", "", console).commandLoop();
    }
}
