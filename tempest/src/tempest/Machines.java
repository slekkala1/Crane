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
    private final List<Machine> machines = new ArrayList<>();
    private final Machine localMachine;

    public Machines() throws UnknownHostException {
        this(readPropertiesFile().split(","), 4444);
    }

    public Machines(String[] machines, int localPort) throws UnknownHostException {
        for(String machine : machines) {
            String[] split = machine.split(":");
            this.machines.add(new Machine(split[0], Integer.parseInt(split[1])));
        }
        localMachine = new Machine(Inet4Address.getLocalHost().getHostName(), localPort);
        if (getMachineNumber() == -1)
            this.machines.add(localMachine);
    }

    public int getMachineNumber() {
        int i = 0;
        for (Machine machine : machines) {
            if (machine.getHostName() == localMachine.getHostName() && machine.getPort() == localMachine.getPort())
                return i;
            ++i;
        }
        return -1;
    }

    public Machine[] getMachines() {
        return machines.toArray(new Machine[machines.size()]);
    }

    private static String readPropertiesFile() {
        Properties prop = new Properties();

        try (InputStream inputStream = Machines.class.getClassLoader().getResourceAsStream("config.properties")) {

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
