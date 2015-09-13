package tempest;

import static org.junit.Assert.*;
import org.junit.Test;

import java.net.Inet4Address;
import java.net.UnknownHostException;

public class MachinesTest {
    @Test
    public void machinesReadsPropertiesFile() throws UnknownHostException {
        Machines machines = new Machines();
        assertEquals(8, machines.getMachines().length);
        assertEquals("fa15-cs425-g03-01.cs.illinois.edu", machines.getMachines()[0].getHostName());
        assertEquals(4444, machines.getMachines()[0].getPort());
    }

    @Test
    public void machinesAddsRunningMachineIfNotInProperties() throws UnknownHostException {
        Machines machines = new Machines();
        assertEquals(8, machines.getMachines().length);
        assertEquals("fa15-cs425-g03-01.cs.illinois.edu", machines.getMachines()[0].getHostName());
        assertEquals(4444, machines.getMachines()[0].getPort());
        assertEquals(Inet4Address.getLocalHost().getHostName(), machines.getMachines()[7].getHostName());
        assertEquals(4444, machines.getMachines()[1].getPort());
    }

    @Test
    public void getMachineNumberMatchesLocalMachine() throws UnknownHostException {
        Machines machines = new Machines();
        assertEquals(7, machines.getMachineNumber());
    }
}
