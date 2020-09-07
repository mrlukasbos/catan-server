import org.java_websocket.WebSocket;
import java.nio.channels.AsynchronousSocketChannel;

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

    AsynchronousSocketChannel getSocket() {
        return null;
    }

    boolean isClosed() {
        return !isOpen();
    }

    ConnectionType getType() {
        return type;
    }
}
