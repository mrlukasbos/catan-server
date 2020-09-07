import org.java_websocket.WebSocket;

import java.net.Socket;

enum ConnectionType {
    NONE,
    SOCKET,
    WEBSOCKET,
}

abstract class Connection {
    ConnectionType type = ConnectionType.NONE;

    abstract boolean isOpen();
    abstract void send(String message);
    abstract void close();

    WebSocket getWebSocket() {
        return null;
    }

    Socket getSocket() {
        return null;
    }

    boolean isClosed() {
        return !isOpen();
    }

    ConnectionType getType() {
        return type;
    }
}
