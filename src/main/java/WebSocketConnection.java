import org.java_websocket.WebSocket;

import java.net.Socket;

public class WebSocketConnection extends Connection {
    ConnectionType type = ConnectionType.WEBSOCKET;
    WebSocket webSocket;

    WebSocketConnection(WebSocket webSocket) {
        this.webSocket = webSocket;
    }

    boolean isOpen() {
        return webSocket.isOpen();
    }

    void send(String message) {
         webSocket.send(message);
    }

    void close() {
        webSocket.close();
    }

    @Override
    WebSocket getWebSocket() {
        return webSocket;
    }
}
