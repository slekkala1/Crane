package tempest;

import static org.junit.Assert.*;
import org.junit.Test;

import java.net.Inet4Address;
import java.net.UnknownHostException;

public class MachinesTest {
    @Test
    public void machinesReadsPropertiesFile() throws UnknownHostException {
        Machines machines = new Machines();
        assertEquals(2, machines.getMachines().length);
        assertEquals("not-this-machine.com", machines.getMachines()[0]);
    }

    @Test
    public void machinesAddsRunningMachineIfNotInProperties() throws UnknownHostException {
        Machines machines = new Machines();
        assertEquals(2, machines.getMachines().length);
        assertEquals("not-this-machine.com", machines.getMachines()[0]);
        assertEquals(Inet4Address.getLocalHost().getHostName(), machines.getMachines()[1]);
    }

    @Test
    public void getMachineNumberMatchesLocalMachine() throws UnknownHostException {
        Machines machines = new Machines();
        assertEquals(1, machines.getMachineNumber());
    }
}
