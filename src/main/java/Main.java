public class Main {

    public static void main(String[] args) {
        SocketServer socketServer = new SocketServer(10006);
        InterfaceServer iface = new InterfaceServer( 10007);
        GameManager gameManager = new GameManager(socketServer, iface);

        socketServer.start(iface, gameManager);
        iface.start(socketServer, gameManager); // the interface starts/ends the game
    }
}
