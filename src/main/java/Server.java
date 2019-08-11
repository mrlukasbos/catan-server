/*
The Server maintains a TCP connection with the players
 */

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class Server extends Thread {

    private ServerSocket serverSocket;
    private int amountOfConnections = 0;
    private GameManager gm;

    // start a server on this device
    Server(int port, GameManager gm) {
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setSoTimeout(100000);
            this.gm = gm;
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

        // since serverSocket.accept() is blocking we need to check here if the game hasn't started already.
        if (!gm.IsRunning()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(newConnection.getInputStream()));
            String line = reader.readLine();
            System.out.println("Just connected to " + line + " on address: " + newConnection.getRemoteSocketAddress());
            Player newPlayer = new Player(amountOfConnections, line);
            newPlayer.setSocket(newConnection);
            amountOfConnections++;
            gm.getCurrentGame().addPlayer(newPlayer);
        }
    }

    int getAmountOfConnections() {
        return amountOfConnections;
    }

}

