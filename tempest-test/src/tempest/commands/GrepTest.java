package tempest.commands;

import static org.junit.Assert.*;
import org.junit.Test;

import tempest.commands.command.Grep;
import tempest.commands.command.Ping;
import tempest.commands.handler.GrepHandler;
import tempest.mocks.MockLogger;

public class GrepTest {
    @Test
    public void GrepRequest() {
        String request = "dogs";
        Grep Grep = new Grep();
        Grep.setRequest(request);
        assertEquals(request, Grep.getRequest());
    }

    @Test
    public void GrepResponse() {
        String response = "dogs love trucks";
        Grep Grep = new Grep();
        Grep.setResponse(response);
        assertEquals(response, Grep.getResponse());
    }

    @Test
    public void GrepHandlerSerialize() {
        String request = "dogs";
        String response = "dogs love trucks";
        Grep Grep = new Grep();
        Grep.setRequest(request);
        Grep.setResponse(response);
        GrepHandler GrepHandler = new GrepHandler(new MockLogger());
        String serializedGrep = GrepHandler.serialize(Grep);
        String expectedResult = "dogs" + System.lineSeparator() + "dogs love trucks";
        assertEquals(expectedResult, serializedGrep);
    }

    @Test
    public void GrepHandlerDeserialize() {
        String serializedRequest = "cats";
        String serializedResponse = "cats hate water";
        GrepHandler GrepHandler = new GrepHandler(new MockLogger());
        Grep Grep = GrepHandler.deserialize(serializedRequest, serializedResponse);
        assertEquals("cats", Grep.getRequest());
        assertEquals("cats hate water", Grep.getResponse());
    }

    @Test
    public void GrepHandlerCanHandle() {
        GrepHandler GrepHandler = new GrepHandler(new MockLogger());
        assertTrue(GrepHandler.canHandle(new Grep().getCommandId()));
        assertFalse(GrepHandler.canHandle(new Ping().getCommandId()));
    }

    @Test
    public void GrepHandlerExecute() {
        MockLogger mockLogger = new MockLogger();
        mockLogger.grep = "dogs love trucks";
        GrepHandler GrepHandler = new GrepHandler(mockLogger);
        assertEquals(mockLogger.grep, GrepHandler.execute("dogs"));
    }

    @Test
    public void GrepAdd() {
        String response1 = "Response 1";
        String response2 = "Response 2";
        String expectedResult = response1 + System.lineSeparator() + response2;
        Grep Grep = new Grep();
        assertEquals(expectedResult, Grep.add(response1, response2));
    }
}
