package tempest.services;

import static org.junit.Assert.*;
import org.junit.Test;
import tempest.Machines;
import tempest.commands.response.Response;
import tempest.mocks.MockExecutor;
import tempest.mocks.MockLogWrapper;

import java.io.IOException;
import java.net.Inet4Address;

public class ClientServerTest {
    @Test
    public void distributedPingAll() throws IOException {
        String hostTemplate = Inet4Address.getLocalHost().getHostName() + ":";
        Machines machines = new Machines(new String[] {
                hostTemplate + 1111,
                hostTemplate + 2222,
                hostTemplate + 3333,
                hostTemplate + 4444,
                hostTemplate + 5555,
                hostTemplate + 6666,
                hostTemplate + 7777
        }, 1111);
        Logger logger = new Logger(new MockExecutor(), new MockLogWrapper(), "logfile.log", "logfile.log");
        Server server1 = new Server(logger, 1111);
        Server server2 = new Server(logger, 2222);
        Server server3 = new Server(logger, 3333);
        Server server4 = new Server(logger, 4444);
        Server server5 = new Server(logger, 5555);
        Server server6 = new Server(logger, 6666);
        Server server7 = new Server(logger, 7777);

        server1.start();
        server2.start();
        server3.start();
        server4.start();
        server5.start();
        server6.start();
        server7.start();

        Client client = new Client(machines, logger);
        Response response = client.pingAll();

        assertEquals(7, response.getLineCount());
    }
}
