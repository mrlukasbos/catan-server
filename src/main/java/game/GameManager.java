package game;

import communication.SocketConnectionServer;
import communication.WebSocketConnectionServer;

public class GameManager {
    Game game;
    SocketConnectionServer server;
    WebSocketConnectionServer iface;

    public GameManager(SocketConnectionServer server, WebSocketConnectionServer iface) {
        this.server = server;
        this.iface = iface;
        game = new Game(iface);
    }

    public void startGame() {
        game.startGame();
    }

    public void stopGame() {
        game.quit();
        game = new Game(iface);
//        iface.clearConnections();
//        server.clearConnections();
    }

    public Game getCurrentGame() {
        return game;
    }
}
