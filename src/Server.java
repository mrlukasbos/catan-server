import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

public class Server extends Thread {

    private ServerSocket serverSocket;
    private ArrayList<Socket> sockets = new ArrayList<Socket>();
    private int amountOfConnections;
    private boolean hasEnoughConnections = false;

    // start a server on this device
    public Server(int port, int amountOfConnections) throws IOException {
        serverSocket = new ServerSocket(port);
        serverSocket.setSoTimeout(100000);
        this.amountOfConnections = amountOfConnections;
    }

    public void run() {
        while (true) {
            try {
<<<<<<< HEAD
=======
                System.out.println("Waiting for clients on port " + serverSocket.getLocalPort() + "...");
                Socket server = serverSocket.accept();
>>>>>>> 4913f051564eb6810f0f217b785a8a7b1bcd45d2

                // this is blocking
                if (!hasEnoughPlayers()) {
                    System.out.println("Waiting for clients on port " +  serverSocket.getLocalPort() + "...");
                    ensureConnections();
                }

                // At this point we are guaranteed to have enough players connected


            } catch (SocketTimeoutException s) {
                System.out.println("Socket timed out!");
                break;
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }

<<<<<<< HEAD

    public void shutDown() {
        for (Socket socket : sockets) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    private void ensureConnections() throws IOException {
        // the sockets we get from the server need to be assigned to players
        for (int i = 0; i < amountOfConnections; i++) {
            Socket listener = serverSocket.accept();
            sockets.add(listener);

            System.out.println("Just connected to " + listener.getRemoteSocketAddress());

            DataOutputStream out = new DataOutputStream(listener.getOutputStream());
            out.writeUTF("You are connected to Catan server: " + listener.getLocalSocketAddress());

            BufferedReader reader = new BufferedReader(new InputStreamReader(listener.getInputStream()));
            String line = reader.readLine();    // reads a line of text
            System.out.println("Ready to receive ");
            System.out.println("I received something: " + line);
=======
    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);
        try {
            Thread t = new Server(port);
            t.start();
        } catch (IOException e) {
            e.printStackTrace();
>>>>>>> 4913f051564eb6810f0f217b785a8a7b1bcd45d2
        }
        hasEnoughConnections = true;
    }

    boolean hasEnoughPlayers() {
        return hasEnoughConnections;
    }
}