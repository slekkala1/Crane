package tempest.commands;

import org.junit.Test;
import tempest.commands.command.Grep;
import tempest.mocks.MockLogger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GrepTest {
//    @Test
//    public void ClientGrepRequest() {
//        Grep grep = new Grep();
//        grep.setRequest("monkeys");
//        assertEquals("grep monkeys", grep.getRequest());
//    }
//
//    @Test
//    public void ServerGrepResponse() {
//        MockLogger logger = new MockLogger();
//        logger.grep = "I hate it when monkeys throw their poop at me!";
//        tempest.commands.server.Grep serverGrep = new tempest.commands.server.Grep(logger);
//        assertEquals(logger.grep, serverGrep.execute("grep monkeys"));
//    }
//
//    @Test
//    public void ServerGrepHandlesClientClient() {
//        MockLogger logger = new MockLogger();
//        logger.grep = "I hate it when monkeys throw their poop at me!";
//        Grep grep = new Grep("monkeys");
//        tempest.commands.server.Grep serverGrep = new tempest.commands.server.Grep(logger);
//        assertTrue(serverGrep.canExecute(grep.getRequest()));
//    }
//
//    @Test
//    public void ClientPingResponse() {
//        MockLogger logger = new MockLogger();
//        logger.grep = "I hate it when monkeys throw their poop at me!";
//        Grep grep = new Grep("monkeys");
//        tempest.commands.server.Grep serverGrep = new tempest.commands.server.Grep(logger);
//        ResponseData response = grep.getResponse(serverGrep.execute(grep.getRequest()), 1, 2);
//        assertEquals(logger.grep, response.getResponse());
//        assertEquals(1, response.getLineCount());
//        assertEquals(2, response.getQueryLatency());
//    }
}
