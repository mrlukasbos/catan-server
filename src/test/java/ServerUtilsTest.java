import communication.Connection;
import communication.ServerUtils;
import game.GameManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ServerUtilsTest {
    GameManager manager = new GameManager(null, null);
    ServerUtils serverUtils = new ServerUtils(manager);
    dummyConnection dummyConnection = new dummyConnection();

    @Test
    void itHandlesConnections() {
        assertEquals(0, dummyConnection.amountClosed);
        assertEquals(0, dummyConnection.receivedMessages);
        serverUtils.handleConnect(dummyConnection);
        assertEquals(1, dummyConnection.receivedMessages);
    }

    @Test
    void itHandlesJoinAndLeaveMessages() {
        serverUtils.handleConnect(dummyConnection);
        assertEquals(0, serverUtils.getRegisteredConnections().size());
        serverUtils.handleMessage(dummyConnection, "{ \"model\": \"join\", \"attributes\": { \"id\": 0, \"name\": \"Test\" } }");
        assertEquals(1, serverUtils.getRegisteredConnections().size());
        assertEquals("Test", serverUtils.getRegisteredConnections().get(0).getName());
        assertEquals(1, serverUtils.getGameManager().getCurrentGame().getPlayers().size());

        serverUtils.handleMessage(dummyConnection, "{ \"model\": \"leave\", \"attributes\": { \"id\": 0 } }");
        assertEquals(0, serverUtils.getGameManager().getCurrentGame().getPlayers().size());
    }
}

class dummyConnection extends Connection {
    public int receivedMessages = 0;
    public int amountClosed = 0;

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public void send(String message) {
        receivedMessages++;
    }

    @Override
    public void close() {
        amountClosed--;
    }

    @Override
    public boolean equals(Connection other) {
        return true;
    }
}