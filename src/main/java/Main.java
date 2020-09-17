import communication.SocketConnectionServer;
import communication.WebSocketConnectionServer;
import game.GameManager;

public class Main {

    public static void main(String[] args) {
        SocketConnectionServer socketConnectionServer = new SocketConnectionServer(10006);
        WebSocketConnectionServer iface = new WebSocketConnectionServer( 10007);
        GameManager gameManager = new GameManager(socketConnectionServer, iface);

        socketConnectionServer.start(gameManager);
        iface.start(gameManager); // the interface starts/ends the game
    }
}
