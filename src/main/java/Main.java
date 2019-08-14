public class Main {

    public static void main(String[] args) {
        Server server = new Server(10006);
        Interface iface = new Interface( 10007);
        Game game = new Game(iface, server);

        server.start(iface, game);
        iface.start(server, game); // the interface starts/ends the game
    }
}
