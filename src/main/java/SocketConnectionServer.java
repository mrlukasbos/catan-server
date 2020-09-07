/*
    Server that maintains connections with players
 */

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class SocketConnectionServer extends Thread {

    private ServerSocket serverSocket;
    private GameManager gameManager;
    private ArrayList<Player> connections = new ArrayList<Player>();
    private WebSocketConnectionServer iface;

    // start a server on this device
    SocketConnectionServer(int port) {
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setSoTimeout(0);
            print("Players can connect to: " + InetAddress.getLocalHost() + ":" + serverSocket.getLocalPort() + "...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Start the server thread for a given game
    void start(WebSocketConnectionServer iface, GameManager gameManager) {
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
                listen();
            } catch (SocketTimeoutException s) {
                print("Socket timed out");
                break;
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    private void listen() {
        for (Player connectedPlayer : connections) {
            Connection connection = connectedPlayer.getConnection();

            if (connection.getSocket() != null && connection.isOpen()) {
                try {
                    if (connection.getSocket().getInputStream().available() > 2) {
                        BufferedInputStream bf = new BufferedInputStream(connection.getSocket().getInputStream());
                        BufferedReader r = new BufferedReader(new InputStreamReader(bf, StandardCharsets.UTF_8));
                        r.readLine();
                        connectedPlayer.setBufferedReply(r.readLine());
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    // Ensures connections with the players
    // A player has to connect and return a string immediately (the string will be the name of the player in-game)
    private void ensureConnections() throws IOException {


        SocketConnection connection = new SocketConnection(serverSocket.accept());
        Player newPlayer = new Player(connection, gameManager.getCurrentGame(), gameManager.getCurrentGame().getPlayers().size(), "playername");
        connections.add(newPlayer);

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

