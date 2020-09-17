import java.nio.channels.AsynchronousSocketChannel;

public class SocketConnectionServer extends SocketConnectionManager {
    private ServerUtils serverUtils;

    SocketConnectionServer(int port) {
        super(port);
    }

    // Start the server thread for a given game
    void start(GameManager gameManager) {
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
