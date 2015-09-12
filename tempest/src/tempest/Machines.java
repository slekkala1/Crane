package tempest;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class Machines {
    private final List<String> machines;
    private final String localMachine;

    public Machines() throws UnknownHostException {
        machines = new ArrayList<>(Arrays.asList(readPropertiesFile().split(",")));
        localMachine = Inet4Address.getLocalHost().getHostName();
        if (getMachineNumber() == -1)
            machines.add(localMachine);
    }

    public int getMachineNumber() {
        return machines.indexOf(localMachine);
    }

    public String[] getMachines() {
        return machines.toArray(new String[machines.size()]);
    }

    private String readPropertiesFile() {
        Properties prop = new Properties();

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("config.properties")) {

            if (inputStream != null) {
                prop.load(inputStream);
            } else {
                throw new FileNotFoundException("property file 'config.properties' not found in the classpath");
            }
        } catch (Exception e) {
            return "";
        }
        return prop.getProperty("machines");
    }
}
