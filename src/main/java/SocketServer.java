/*
    Server that maintains connections with players
 */

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

public class SocketServer extends Thread {

    private ServerSocket serverSocket;
    private GameManager gameManager;
    private ArrayList<Player> connections = new ArrayList<Player>();
    private InterfaceServer iface;

    // start a server on this device
    SocketServer(int port) {
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setSoTimeout(0);
            print("Players can connect to: " + InetAddress.getLocalHost() + ":" + serverSocket.getLocalPort() + "...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Start the server thread for a given game
    void start(InterfaceServer iface, GameManager gameManager) {
        this.gameManager = gameManager;
        this.iface = iface;
        start();
    }

    // This function gets called automatically by calling start();
    // The server thread will constantly run this: ensuring connections with the players
    public void run() {
        while (true) {
            try {
                ensureConnections();
            } catch (SocketTimeoutException s) {
                print("Socket timed out");
                break;
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    // Ensures connections with the players
    // A player has to connect and return a string immediately (the string will be the name of the player in-game)
    private void ensureConnections() throws IOException {
        // the sockets we get from the server need to be assigned to players
        Socket newConnection = serverSocket.accept();
        BufferedReader reader = new BufferedReader(new InputStreamReader(newConnection.getInputStream()));
        String line = reader.readLine();

        PlayerSocket newPlayer = new PlayerSocket(gameManager.getCurrentGame(), gameManager.getCurrentGame().getPlayers().size(), line);
        newPlayer.setSocket(newConnection);
        connections.add(newPlayer);
        print("Just connected to " + line + " on address: " + newConnection.getRemoteSocketAddress());

        if (!gameManager.getCurrentGame().isRunning()) {
            gameManager.getCurrentGame().addPlayer(newPlayer);
            Response idAcknowledgement = Constants.ID_ACK.withAdditionalInfo("" + newPlayer.getId());
            newPlayer.send(idAcknowledgement.toString());
        }
    }

    ArrayList<Player> getConnections() {
        return connections;
    }

    void clearConnections() {
        connections.clear();
    }

    private void print(String msg) {
        System.out.println("[Server] \t" + msg);
    }
}

