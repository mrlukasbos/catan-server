import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        Game game = new Game(1);
        try {

            // Boot the server
            // the server will first make connections with the amount of players we want
            Server server = new Server(10006, game);
            server.start();

            Sock s = new Sock( 10007);
            s.start();
            System.out.println( "Visualization started on " + s.getAddress() + s.getPort() );


            // main thread has to wait now to make sure the server has enough players connected
            while(!server.hasEnoughPlayers()) Thread.sleep(1000);

            while (true) {
                s.broadcast(game.getBoard().toString());
                System.out.println( "broadcasting visuals on" + s.getAddress() + s.getPort() );

                Thread.sleep(3000);
            }

          //  game.start();




            // server.shutDown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
