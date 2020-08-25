public class GameManager {
    Game game;
    SocketServer server;
    InterfaceServer iface;

    GameManager(SocketServer server, InterfaceServer iface) {
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
    }

    public Game getCurrentGame() {
        return game;
    }
}
