public class Main {

    public static void main(String[] args) {
        SocketConnectionServer socketConnectionServer = new SocketConnectionServer(10006);
        WebSocketConnectionServer iface = new WebSocketConnectionServer( 10007);
        GameManager gameManager = new GameManager(socketConnectionServer, iface);

        socketConnectionServer.start(iface, gameManager);
        iface.start(socketConnectionServer, gameManager); // the interface starts/ends the game
    }
}
