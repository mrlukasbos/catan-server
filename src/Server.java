import java.io.*;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;

public class Server extends Thread {

    private ServerSocket serverSocket;
    private boolean hasEnoughConnections = false;
    private Game game;

    // start a server on this device
    public Server(int port, Game game) throws IOException {
        serverSocket = new ServerSocket(port);
        serverSocket.setSoTimeout(100000);
        this.game = game;
    }

    public void run() {
        while (true) {
            try {

                // this is blocking
                if (!hasEnoughPlayers()) {
                    System.out.println("Waiting for clients on port " +  serverSocket.getLocalPort() + "...");
                    ensureConnections();
                }


                System.out.println("Playing the game!");
                sendBoardData();


            } catch (SocketTimeoutException s) {
                System.out.println("Socket timed out!");
                break;
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    public void shutDown() {
        for (Player p : game.getPlayers()) {
            try {
                p.getSocket().close();
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    private void ensureConnections() throws IOException {
        // the sockets we get from the server need to be assigned to players
        for (Player p : game.getPlayers()) {
            p.setSocket(serverSocket.accept());
            System.out.println("Just connected to " + p.getSocket().getRemoteSocketAddress());
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getSocket().getInputStream()));
            String line = reader.readLine();

            System.out.println("Ready to receive ");
            System.out.println("I received something: " + line);

            DataOutputStream out = new DataOutputStream(p.getSocket().getOutputStream());
            out.writeUTF(line);
        }
        hasEnoughConnections = true;
    }

    public void sendBoardData() {


        for (Player p : game.getPlayers()) {
            // At this point we are guaranteed to have enough players connected
            try {
                DataOutputStream out = new DataOutputStream(p.getSocket().getOutputStream());
                out.writeUTF(game.getBoard().toString());
            } catch (IOException e) {
            e.printStackTrace();
            break;
        }
        }
    }

    boolean hasEnoughPlayers() {
        return hasEnoughConnections;
    }
}