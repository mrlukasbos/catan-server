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

public abstract class SocketConnectionManager extends Thread {
    private AsynchronousServerSocketChannel server;
    private ArrayList<ConnectionElement> connections = new ArrayList<>();
    private Future<AsynchronousSocketChannel> acceptCon;

    abstract void onOpen(AsynchronousSocketChannel conn);
    abstract void onClose(AsynchronousSocketChannel conn);
    abstract void onMessage(AsynchronousSocketChannel conn, String message);

    // start a server on this device
    SocketConnectionManager(int port) {
        try {
            server = AsynchronousServerSocketChannel.open();
            server.bind(new InetSocketAddress(port));
            acceptCon = server.accept(); // set acceptCon
        } catch (IOException e) {
            e.printStackTrace();
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
                        connection.result.cancel(true);
                        toRemove = connection;
                    }

                    // execute the messages line by line
                    String receivedMessage = new String(connection.buffer.array()).trim();
                    for (String msg : receivedMessage.split("\r\n")) {
                        if (msg.isEmpty()) {
                            toRemove = connection;
                            break;
                        }
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
            if (acceptCon.isDone()) {
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
}

