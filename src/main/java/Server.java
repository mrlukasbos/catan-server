/*
The Server maintains a TCP connection with the players
 */

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

public class Server extends Thread {

    private ServerSocket serverSocket;
    private Game game;
    private ArrayList<Player> connections = new ArrayList<Player>();

    // start a server on this device
    Server(int port) {
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setSoTimeout(100000);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        while (true) {
            try {
                System.out.println("Players can connect to: " + InetAddress.getLocalHost() + ":" + serverSocket.getLocalPort() + "...");
                ensureConnections();
            } catch (SocketTimeoutException s) {
                System.out.println("Socket timed out!");
                break;
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    void shutDown() {
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
        Socket newConnection = serverSocket.accept();
        BufferedReader reader = new BufferedReader(new InputStreamReader(newConnection.getInputStream()));
        String line = reader.readLine();

        Player newPlayer = new Player(connections.size(), line);
        newPlayer.setSocket(newConnection);
        connections.add(newPlayer);
        System.out.println("[Server] Just connected to " + line + " on address: " + newConnection.getRemoteSocketAddress());
    }

    ArrayList<Player> getConnections() {
        return connections;
    }

}

