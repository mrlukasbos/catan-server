import java.io.*;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class SocketConnectionServer extends Thread {
    AsynchronousServerSocketChannel channel ;
    private GameManager gameManager;
    private ArrayList<Player> connections = new ArrayList<Player>();
    private WebSocketConnectionServer iface;

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
                listen();
            } catch (Exception e) {
                e.printStackTrace();
            }

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

    private void listen() throws ExecutionException, InterruptedException {
        print("listening...");

        for (Player connectedPlayer : connections) {
            Connection connection = connectedPlayer.getConnection();
            print("listening to player ID: " + connectedPlayer.getId());

            if (connection.getSocket() != null && connection.isOpen()) {

                ByteBuffer buffer = ByteBuffer.allocate(1024);

                Future<Integer> readval = connection.getSocket().read(buffer);
                while(!readval.isDone()) {
                    Thread.sleep(100);
                }
                if(readval.isDone()) {
                    readval.get();
                    String receivedMessage = new String(buffer.array()).trim();
                    System.out.println("Received from client: " + receivedMessage);
                    connectedPlayer.setBufferedReply(receivedMessage);
                }
            }
        }
    }

    // Ensures connections with the players
    // A player has to connect and return a string immediately (the string will be the name of the player in-game)
    private void ensureConnections() throws IOException {
        print("ensureConnections...");

        try {
            Future<AsynchronousSocketChannel> acceptCon = channel.accept();

            // try to get a response. if the game is started we must cancel the new player.
            while(!acceptCon.isDone() && !gameManager.getCurrentGame().isRunning()) {
                Thread.sleep(300);
            }
            if (gameManager.getCurrentGame().isRunning()) {
                acceptCon.cancel(true);
                return;
            }

            AsynchronousSocketChannel client = acceptCon.get();

            if ((client!= null) && (client.isOpen())) {
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                Future<Integer> readval = client.read(buffer);

                // try to get a response. if the game is started we must cancel the new player.
                while(!readval.isDone() && !gameManager.getCurrentGame().isRunning()) {
                    Thread.sleep(300);
                }
                if (gameManager.getCurrentGame().isRunning()) {
                    readval.cancel(true);
                    return;
                }
                readval.get();

                String receivedMessage = new String(buffer.array()).trim();
                System.out.println("Received from client: " + receivedMessage);

                SocketConnection connection = new SocketConnection(client);
                Player newPlayer = new Player(connection, gameManager.getCurrentGame(), gameManager.getCurrentGame().getPlayers().size(), receivedMessage);
                connections.add(newPlayer);

                if (!gameManager.getCurrentGame().isRunning()) {
                    gameManager.getCurrentGame().addPlayer(newPlayer);
                    Response idAcknowledgement = Constants.ID_ACK.withAdditionalInfo("" + newPlayer.getId());
                    newPlayer.send(idAcknowledgement.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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

