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
                hostTemplate + 4441,
                hostTemplate + 4442,
                hostTemplate + 4443,
                hostTemplate + 4444,
                hostTemplate + 4445,
                hostTemplate + 4446,
                hostTemplate + 4447
        }, 4441);
        Logger logger = new Logger(new MockExecutor(), new MockLogWrapper(), "logfile.log", "logfile.log");
        Server server1 = new Server(logger, 4441);
        Server server2 = new Server(logger, 4442);
        Server server3 = new Server(logger, 4443);
        Server server4 = new Server(logger, 4444);
        Server server5 = new Server(logger, 4445);
        Server server6 = new Server(logger, 4446);
        Server server7 = new Server(logger, 4447);

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

        server1.stop();
        server2.stop();
        server3.stop();
        server4.stop();
        server5.stop();
        server6.stop();
        server7.stop();
    }

    @Test
    public void distributedGrepAll() throws IOException {
        String hostTemplate = Inet4Address.getLocalHost().getHostName() + ":";
        Machines machines = new Machines(new String[] {
                hostTemplate + 4441,
                hostTemplate + 4442,
                hostTemplate + 4443,
                hostTemplate + 4444,
                hostTemplate + 4445,
                hostTemplate + 4446,
                hostTemplate + 4447
        }, 4441);
        Logger logger1 = new Logger(new MockExecutor(), new DefaultLogWrapper(), "logfile.log", "logs/vm1.testlog");
        Server server1 = new Server(logger1, 4441);
        Logger logger2 = new Logger(new MockExecutor(), new DefaultLogWrapper(), "logfile.log", "logs/vm2.testlog");
        Server server2 = new Server(logger2, 4442);
        Logger logger3 = new Logger(new MockExecutor(), new DefaultLogWrapper(), "logfile.log", "logs/vm3.testlog");
        Server server3 = new Server(logger3, 4443);
        Logger logger4 = new Logger(new MockExecutor(), new DefaultLogWrapper(), "logfile.log", "logs/vm4.testlog");
        Server server4 = new Server(logger4, 4444);
        Logger logger5 = new Logger(new MockExecutor(), new DefaultLogWrapper(), "logfile.log", "logs/vm5.testlog");
        Server server5 = new Server(logger5, 4445);
        Logger logger6 = new Logger(new MockExecutor(), new DefaultLogWrapper(), "logfile.log", "logs/vm6.testlog");
        Server server6 = new Server(logger6, 4446);
        Logger logger7 = new Logger(new MockExecutor(), new DefaultLogWrapper(), "logfile.log", "logs/vm7.testlog");
        Server server7 = new Server(logger7, 4447);

        Machines machinesFull = new Machines(new String[] { hostTemplate + 4448 }, 4448);
        Logger loggerFull = new Logger(new MockExecutor(), new DefaultLogWrapper(), "logfile.log", "logs/vmfull.testlog");
        Server serverFull = new Server(loggerFull, 4448);

        server1.start();
        server2.start();
        server3.start();
        server4.start();
        server5.start();
        server6.start();
        server7.start();

        serverFull.start();

        Client client = new Client(machines, logger1);
        Response response = client.grepAll("money");

        Client clientFull = new Client(machinesFull, loggerFull);
        Response responseFull = clientFull.grep(machinesFull.getLocalMachine(), "money");

        System.out.println("Full response lines: " + responseFull.getLineCount());
        System.out.println("Distributed response lines: " + response.getLineCount());
        assertEquals(responseFull.getLineCount(), response.getLineCount());

        server1.stop();
        server2.stop();
        server3.stop();
        server4.stop();
        server5.stop();
        server6.stop();
        server7.stop();
        serverFull.stop();
    }
}
