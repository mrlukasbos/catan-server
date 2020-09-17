import org.junit.jupiter.api.BeforeEach;
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
    void itHandlesJoinMessages() {
        serverUtils.handleConnect(dummyConnection);
        assertEquals(0, serverUtils.registeredConnections.size());
        serverUtils.handleMessage(dummyConnection, "{ \"model\": \"join\", \"attributes\": { \"id\": 0, \"name\": \"Test\" } }");
        assertEquals(1, serverUtils.registeredConnections.size());
    }


}

class dummyConnection extends Connection {
    public int receivedMessages = 0;
    public int amountClosed = 0;

    @Override
    boolean isOpen() {
        return true;
    }

    @Override
    void send(String message) {
        receivedMessages++;
    }

    @Override
    void close() {
        amountClosed--;
    }

    @Override
    boolean equals(Connection other) {
        return true;
    }
}