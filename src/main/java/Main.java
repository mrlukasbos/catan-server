import java.io.IOException;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        GameManager gm = new GameManager();
        try {

            // Boot the server which will provide sockets for the players that connect
            Server server = new Server(10006, gm);
            server.start();

            // Boot the visualizer websocket
            Sock s = new Sock( 10007, gm, server);
            s.start();
            System.out.println( "Visualization started on " + s.getAddress() + s.getPort() );

            // main thread has to wait now to make sure the server has enough players connected
            while(!gm.IsRunning()) {
                String names = "";
                for (Player p : gm.getCurrentGame().getPlayers()) {
                    names += p.getName() + ",";
                }

                s.broadcast("SYSTEM_MSG:" + "Current connected players: " + names + "(" + server.getAmountOfConnections() + ")");
                Thread.sleep(400);
            }
            gm.run(s, server);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
