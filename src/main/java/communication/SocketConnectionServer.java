package communication;

import game.GameManager;
import java.nio.channels.AsynchronousSocketChannel;

public class SocketConnectionServer extends SocketConnectionManager {
    private ServerUtils serverUtils;
    private int port;

    public SocketConnectionServer(int port) {
        super(port);
        this.port = port;
    }

    // Start the server thread for a given game
    public void start(GameManager gameManager) {
        System.out.println("Starting SocketConnectionServer on port " + port);
        serverUtils = new ServerUtils(gameManager);
        start();
    }

    @Override
    public void onOpen(AsynchronousSocketChannel conn) {
        serverUtils.handleConnect(new SocketConnection(conn));
    }

    @Override
    public void onClose(AsynchronousSocketChannel conn) {
        serverUtils.handleDisconnect(new SocketConnection(conn));
    }

    @Override
    public void onMessage(AsynchronousSocketChannel conn, String message) {
        serverUtils.handleMessage(new SocketConnection(conn), message);
    }
}
