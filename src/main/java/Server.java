import java.io.*;
import java.net.ServerSocket;
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
                if (!hasEnoughPlayers()) {
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
        for (Player p : gm.getCurrentGame().getPlayers()) {
            p.setSocket(serverSocket.accept());
            System.out.println("Just connected to " + p.getSocket().getRemoteSocketAddress());
            amountOfConnections++;
//            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getSocket().getInputStream()));
//            String line = reader.readLine();
//            System.out.println("Ready to receive ");
//            System.out.println("I received something: " + line);
//            DataOutputStream out = new DataOutputStream(p.getSocket().getOutputStream());
//            out.writeUTF(line);
        }
    }

    boolean hasEnoughPlayers() {
        return amountOfConnections >= gm.getCurrentGame().getPlayers().size();
    }
}