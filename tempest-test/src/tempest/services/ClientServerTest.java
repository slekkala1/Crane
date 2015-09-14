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
                hostTemplate + 5541,
                hostTemplate + 5542,
                hostTemplate + 5543,
                hostTemplate + 5544,
                hostTemplate + 5545,
                hostTemplate + 5546,
                hostTemplate + 5547
        }, 5541);
        Logger logger = new Logger(new MockExecutor(), new MockLogWrapper(), "logfile.log", "logfile.log");
        Server server1 = new Server(logger, 5541);
        Server server2 = new Server(logger, 5542);
        Server server3 = new Server(logger, 5543);
        Server server4 = new Server(logger, 5544);
        Server server5 = new Server(logger, 5545);
        Server server6 = new Server(logger, 5546);
        Server server7 = new Server(logger, 5547);

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
                hostTemplate + 5441,
                hostTemplate + 5442,
                hostTemplate + 5443,
                hostTemplate + 5444,
                hostTemplate + 5445,
                hostTemplate + 5446,
                hostTemplate + 5447
        }, 5441);
        Logger logger1 = new Logger(new CommandLineExecutor(), new DefaultLogWrapper(), "logfile.log", "logs/vm1.testlog");
        Server server1 = new Server(logger1, 5441);
        Logger logger2 = new Logger(new CommandLineExecutor(), new DefaultLogWrapper(), "logfile.log", "logs/vm2.testlog");
        Server server2 = new Server(logger2, 5442);
        Logger logger3 = new Logger(new CommandLineExecutor(), new DefaultLogWrapper(), "logfile.log", "logs/vm3.testlog");
        Server server3 = new Server(logger3, 5443);
        Logger logger4 = new Logger(new CommandLineExecutor(), new DefaultLogWrapper(), "logfile.log", "logs/vm4.testlog");
        Server server4 = new Server(logger4, 5444);
        Logger logger5 = new Logger(new CommandLineExecutor(), new DefaultLogWrapper(), "logfile.log", "logs/vm5.testlog");
        Server server5 = new Server(logger5, 5445);
        Logger logger6 = new Logger(new CommandLineExecutor(), new DefaultLogWrapper(), "logfile.log", "logs/vm6.testlog");
        Server server6 = new Server(logger6, 5446);
        Logger logger7 = new Logger(new CommandLineExecutor(), new DefaultLogWrapper(), "logfile.log", "logs/vm7.testlog");
        Server server7 = new Server(logger7, 5447);

        Machines machinesFull = new Machines(new String[] { hostTemplate + 5448 }, 5448);
        Logger loggerFull = new Logger(new MockExecutor(), new DefaultLogWrapper(), "logfile.log", "logs/vmfull.testlog");
        Server serverFull = new Server(loggerFull, 5448);

        server1.start();
        server2.start();
        server3.start();
        server4.start();
        server5.start();
        server6.start();
        server7.start();

        serverFull.start();

        Client client = new Client(machines, logger1);
        Response response = client.grepAll("catalog");

        Client clientFull = new Client(machinesFull, loggerFull);
        Response responseFull = clientFull.grep(machinesFull.getLocalMachine(), "catalog");

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
