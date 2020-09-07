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
    AsynchronousServerSocketChannel channel ;
    private GameManager gameManager;
    private ArrayList<Player> connectedPlayers = new ArrayList<>();
    private ArrayList<ConnectionElement> connections = new ArrayList<>();

    // start a server on this device
    SocketConnectionServer(int port) {
        try {
            channel = AsynchronousServerSocketChannel.open();
            channel.bind(new InetSocketAddress(port));
            print("Players can connect to: " + channel.getLocalAddress() + "...");
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

        if (!gameManager.getCurrentGame().isRunning()) {
            gameManager.getCurrentGame().addPlayer(newPlayer);
            Response idAcknowledgement = Constants.ID_ACK.withAdditionalInfo("" + newPlayer.getId());
            newPlayer.send(idAcknowledgement.toString());
        }
    }

    /*
     * Called whenever a connection is closed
     */
    private void onClose(AsynchronousSocketChannel conn) {
        for (Player connectedPlayer : connectedPlayers) {
            if (connectedPlayer.getConnection().getSocket() == conn) {
                print("the connection with player " + connectedPlayer.getName() + " is closed!");
            }
        }
    }

    /*
     * Called whenever a message is received on a connection
     */
    private void onMessage(AsynchronousSocketChannel conn, String message) {
        for (Player connectedPlayer : connectedPlayers) {
            if (connectedPlayer.getConnection().getSocket() == conn) {
                connectedPlayer.setBufferedReply(message);
            }
        }
    }

    // This function gets called automatically by calling start();
    // The server thread will constantly run this: ensuring connections with the players
    public void run() {
        while (true) {
            try {
                listen();
                ensureConnections();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /*
    Iterate over all connections
    If a connection has a message, it is set to be the bufferedReply of that player
     */
    private void listen() throws ExecutionException, InterruptedException {
        for (ConnectionElement connection : connections) {
            if (connection.channel != null && connection.channel.isOpen()) {
                if (connection.result.isDone()) {
                    connection.result.get();
                    String receivedMessage = new String(connection.buffer.array()).trim();
                    this.onMessage(connection.channel, receivedMessage);

                    // set up the connection element for the next message
                    connection.buffer.clear();
                    connection.result = connection.channel.read(connection.buffer);
                }
            } else {
                connections.remove(connection);
                this.onClose(connection.channel);
            }use
        }
    }

    // Ensures connections with the players
    // A player has to connect and return a string immediately (the string will be the name of the player in-game)
    // this function will only block if the game has not started yet
    private void ensureConnections() {
        try {
            Future<AsynchronousSocketChannel> acceptCon = channel.accept();

            // try to get a response. if the game is started we must cancel the new player.
            while(!acceptCon.isDone() && !gameManager.getCurrentGame().isRunning()) {
                Thread.sleep(300);
            }

            if (gameManager.getCurrentGame().isRunning()) {
                acceptCon.cancel(true);
            } else {
                AsynchronousSocketChannel client = acceptCon.get();
                if ((client != null) && (client.isOpen())) {

                    ConnectionElement elem = new ConnectionElement();
                    elem.channel = client;
                    elem.buffer = ByteBuffer.allocate(1024);
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
}

