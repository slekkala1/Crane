import asg.cliche.ShellFactory;
import org.apache.log4j.BasicConfigurator;

import java.io.IOException;

/**
 * Created by slekkala on 9/9/15.
 */
public class ClientApp {
    public static void main(String[] args) throws IOException {
        BasicConfigurator.configure();
        Console console = new Console(new GrepClient());
        ShellFactory.createConsoleShell("CS425-MP-Lekkala-Morrow", "", console).commandLoop();
    }
}
