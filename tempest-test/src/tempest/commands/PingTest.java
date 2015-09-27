package tempest.commands;

import static org.junit.Assert.*;
import org.junit.Test;

import tempest.commands.client.Ping;
import tempest.commands.response.Response;

public class PingTest {
    @Test
    public void ClientPingRequest() {
        Ping ping = new Ping();
        assertEquals("ping", ping.getRequest());
    }

    @Test
    public void ServerPingResponse() {
        tempest.commands.server.Ping serverPing = new tempest.commands.server.Ping();
        assertEquals("Hello", serverPing.execute("ping"));
    }

    @Test
    public void ServerPingHandlesClientPing() {
        Ping ping = new Ping();
        tempest.commands.server.Ping serverPing = new tempest.commands.server.Ping();
        assertTrue(serverPing.canExecute(ping.getRequest()));
    }

    @Test
    public void ClientPingResponse() {
        Ping ping = new Ping();
        tempest.commands.server.Ping serverPing = new tempest.commands.server.Ping();
        Response response = ping.getResponse(serverPing.execute(ping.getRequest()), 1, 2);
        assertEquals("Hello", response.getResponse());
        assertEquals(1, response.getLineCount());
        assertEquals(2, response.getQueryLatency());
    }
}
