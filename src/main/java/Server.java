import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class Server extends Thread {

    private ServerSocket serverSocket;

    public int getAmountOfConnections() {
        return amountOfConnections;
    }

    private int amountOfConnections = 0;
    private GameManager gm;

    // start a server on this device
    public Server(int port, GameManager gm) throws IOException {
        serverSocket = new ServerSocket(port);
        serverSocket.setSoTimeout(100000);
        this.gm = gm;
    }

    public void run() {
        while (true) {
            try {

                // this is blocking
                if (!gm.IsRunning()) {
                    System.out.println("Waiting for clients on port " +  serverSocket.getLocalPort() + "...");
                    ensureConnections();
                }

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
        for (Player p : gm.getCurrentGame().getPlayers()) {
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
        Socket newConnection = serverSocket.accept();
        BufferedReader reader = new BufferedReader(new InputStreamReader(newConnection.getInputStream()));
        String line = reader.readLine();
        System.out.println("Just connected to " + line + " on address: " + newConnection.getRemoteSocketAddress());
        Player newPlayer = new Player(amountOfConnections, line);
        newPlayer.setSocket(newConnection);
        amountOfConnections++;
        gm.getCurrentGame().addPlayer(newPlayer);
    }

    boolean hasEnoughPlayers() {
        return amountOfConnections >= gm.getCurrentGame().getPlayers().size();
    }
}