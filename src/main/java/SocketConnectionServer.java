import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

class ConnectionElement {
    AsynchronousSocketChannel channel;
    Future<Integer> result;
    ByteBuffer buffer;
}

public class SocketConnectionServer extends Thread {
    AsynchronousServerSocketChannel server;
    private GameManager gameManager;
    private ArrayList<Player> connectedPlayers = new ArrayList<>();
    private ArrayList<ConnectionElement> connections = new ArrayList<>();
    Future<AsynchronousSocketChannel> acceptCon;

    // start a server on this device
    SocketConnectionServer(int port) {
        try {
            server = AsynchronousServerSocketChannel.open();
            server.bind(new InetSocketAddress(port));
            print("Players can connect to port: " + port + "...");
            acceptCon = server.accept(); // set acceptCon
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Start the server thread for a given game
    void start(GameManager gameManager) {
        this.gameManager = gameManager;
        start();
    }

    /*
     * Called whenever a new connection opens
     */
    private void onOpen(AsynchronousSocketChannel conn) {
        SocketConnection connection = new SocketConnection(conn);
        Player newPlayer = new Player(connection, gameManager.getCurrentGame(), gameManager.getCurrentGame().getPlayers().size(), "no_name_yet");
        connectedPlayers.add(newPlayer);
        print("opened new connection");

        if (!gameManager.getCurrentGame().isRunning()) {
            gameManager.getCurrentGame().addPlayer(newPlayer);
            Response idAcknowledgement = Constants.ID_ACK.withAdditionalInfo("" + newPlayer.getId());
            newPlayer.send(idAcknowledgement.toString());
            print("added new player to game");
        }
    }

    /*
     * Called whenever a connection is closed
     */
    private void onClose(AsynchronousSocketChannel conn) {
        for (Player connectedPlayer : connectedPlayers) {
            if (connectedPlayer.getConnection().getSocket() == conn) {
                print("the connection with player " + connectedPlayer.getName() + " is closed!");
                gameManager.getCurrentGame().removePlayer(connectedPlayer);
            }
        }
    }

    /*
     * Called whenever a message is received on a connection
     */
    private void onMessage(AsynchronousSocketChannel conn, String message) {
        for (Player connectedPlayer : connectedPlayers) {
            if (connectedPlayer.getConnection().getSocket() == conn) {
                print("got message from player " + connectedPlayer.getName() + ": " + message);
                connectedPlayer.setBufferedReply(message);
            }
        }
    }

    // This function gets called automatically by calling start();
    // The server thread will constantly run this: ensuring connections with the players
    public void run() {
        while (true) {
            listen();
            ensureConnections();

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /*
    Iterate over all connections
    If a connection has a message, it is set to be the bufferedReply of that player
     */
    private void listen() {
        ConnectionElement toRemove = null;
        for (ConnectionElement connection : connections) {
            if (connection.channel != null && connection.channel.isOpen()) {
                if (connection.result.isDone()) {
                    try {
                        connection.result.get();
                    } catch (InterruptedException | ExecutionException e) {
                        toRemove = connection;
                        connection.result.cancel(true);
                    }

                    // execute the messages line by line
                    String receivedMessage = new String(connection.buffer.array()).trim();
                    for (String msg : receivedMessage.split("\r\n")) {
                        this.onMessage(connection.channel, msg);
                    }

                    // set up the connection element for the next message
                    connection.buffer = ByteBuffer.allocate(2048);
                    connection.result = connection.channel.read(connection.buffer);
                }
            } else {
                connection.result.cancel(true);
                toRemove = connection;
            }
        }
        if (toRemove != null) {
            this.onClose(toRemove.channel);
            connections.remove(toRemove);
        }
    }

    // Ensures connections with the players
    // A player has to connect and return a string immediately (the string will be the name of the player in-game)
    private void ensureConnections() {
        try {
            if (acceptCon.isDone() && !gameManager.getCurrentGame().isRunning()) {
                AsynchronousSocketChannel client = acceptCon.get();
                acceptCon = server.accept(); // reset acceptCon

                if ((client != null) && (client.isOpen())) {
                    ConnectionElement elem = new ConnectionElement();
                    elem.channel = client;
                    elem.buffer = ByteBuffer.allocate(2048);
                    elem.result = elem.channel.read(elem.buffer);
                    this.connections.add(elem);

                    this.onOpen(client);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void clearConnections() {
        connectedPlayers.clear();
    }

    private void print(String msg) {
        System.out.println("[Server] \t" + msg);
    }

    public ArrayList<Player> getConnectedPlayers() {
        return connectedPlayers;
    }

    public ArrayList<ConnectionElement> getConnections() {
        return connections;
    }
}

