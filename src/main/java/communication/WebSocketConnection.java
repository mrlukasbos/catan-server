package communication;
import org.java_websocket.WebSocket;

public class WebSocketConnection extends Connection {
    WebSocket webSocket;

    public WebSocketConnection(WebSocket webSocket) {
        this.webSocket = webSocket;
    }

    public boolean isOpen() {
        return webSocket.isOpen();
    }

    public void send(String message) {
        if (webSocket.isOpen()) {
            webSocket.send(message);
        }
    }

    public void close() {
        webSocket.close();
    }

    @Override
    public WebSocket getWebSocket() {
        return webSocket;
    }
}
