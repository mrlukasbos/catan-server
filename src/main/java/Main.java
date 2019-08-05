import java.io.IOException;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        Game game = new Game(1);
        try {

            // Boot the server
            // the server will first make connections with the amount of players we want
            Server server = new Server(10006, game);
            server.start();

            // main thread has to wait now to make sure the server has enough players connected
            while(!server.hasEnoughPlayers()) Thread.sleep(1000);

            // start the game
            game.start();

            // server.shutDown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
