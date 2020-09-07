import org.java_websocket.WebSocket;

public class WebSocketConnection extends Connection {
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
