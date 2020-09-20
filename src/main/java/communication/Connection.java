package communication;

import org.java_websocket.WebSocket;
import java.nio.channels.AsynchronousSocketChannel;

public abstract class Connection {

    public abstract boolean isOpen();
    public abstract void send(String message);
    public abstract void close();

    public WebSocket getWebSocket() {
        return null;
    }

    public AsynchronousSocketChannel getSocket() {
        return null;
    }

    public boolean equals(Connection otherConnection) {
            return (getWebSocket() != null && (getWebSocket().equals(otherConnection.getWebSocket()))
                    || (getSocket() != null && (getSocket() == otherConnection.getSocket())));
    }
}
