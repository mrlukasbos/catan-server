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
    private Interface iface;

    // start a server on this device
    Server(int port) {
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setSoTimeout(0);
            print("Players can connect to: " + InetAddress.getLocalHost() + ":" + serverSocket.getLocalPort() + "...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Start the server thread for a given game
    void start(Interface iface, Game game) {
        this.game = game;
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

    // Closes the socket connection with the players
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

    // Ensures connections with the players
    // A player has to connect and return a string immediately (the string will be the name of the player in-game)
    private void ensureConnections() throws IOException {
        // the sockets we get from the server need to be assigned to players
        Socket newConnection = serverSocket.accept();
        BufferedReader reader = new BufferedReader(new InputStreamReader(newConnection.getInputStream()));
        String line = reader.readLine();

        Player newPlayer = new Player(game, game.getPlayers().size(), line);
        newPlayer.setSocket(newConnection);
        connections.add(newPlayer);
        print("Just connected to " + line + " on address: " + newConnection.getRemoteSocketAddress());

        if (!game.isRunning()) {
            game.addPlayer(newPlayer);
            iface.broadcast(game.toString());

            Response idAcknowledgement = Constants.ID_ACK.withAdditionalInfo("" + newPlayer.getId());
            newPlayer.send(idAcknowledgement.toString());
        }

        // hack to immediatly start for testing purposses
    //    game.startGame();

    }

    ArrayList<Player> getConnections() {
        return connections;
    }

    private void print(String msg) {
        System.out.println("[Server] \t" + msg);
    }
}

