import java.io.IOException;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        GameManager gm = new GameManager();
        try {

            // Boot the server
            // the server will first make connections with the amount of players we want
            Server server = new Server(10006, gm);
            server.start();

            Sock s = new Sock( 10007, gm);
            s.start();
            System.out.println( "Visualization started on " + s.getAddress() + s.getPort() );

            // main thread has to wait now to make sure the server has enough players connected
            while(!server.hasEnoughPlayers()) {
                System.out.println( "Waiting for more players to join");
                s.broadcast("SYSTEM_MSG:" + "Current amount of players: " + server.getAmountOfConnections());
                Thread.sleep(1000);
            }

            gm.run(s);
            // game.start();
            // server.shutDown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
