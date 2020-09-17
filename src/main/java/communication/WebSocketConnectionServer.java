package communication;

import java.net.InetSocketAddress;

import game.GameManager;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

public class WebSocketConnectionServer extends WebSocketServer {
    ServerUtils serverUtils;

    public WebSocketConnectionServer(int port) {
        super(new InetSocketAddress(port));
        setReuseAddr(true);
    }

    public void start(GameManager gameManager) {
        serverUtils = new ServerUtils(gameManager);
        start();
    }

    @Override
    public void onStart() {
        setConnectionLostTimeout(10);
    }

    @Override
    public void onOpen( WebSocket conn, ClientHandshake handshake ) {
        serverUtils.handleConnect(new WebSocketConnection(conn));
    }

    @Override
    public void onMessage( WebSocket conn, String message ) {
        serverUtils.handleMessage(new WebSocketConnection(conn), message);
    }

    @Override
    public void onError( WebSocket conn, Exception ex ) {
        ex.printStackTrace();
    }

    @Override
    public void onClose( WebSocket conn, int code, String reason, boolean remote ) {
        serverUtils.handleDisconnect(new WebSocketConnection(conn));
    }
}
