public class Main {

    public static void main(String[] args) {
        SocketServer socketServer = new SocketServer(10006);
        InterfaceServer iface = new InterfaceServer( 10007);
        Game game = new Game(iface);

        socketServer.start(iface, game);
        iface.start(socketServer, game); // the interface starts/ends the game
    }
}
