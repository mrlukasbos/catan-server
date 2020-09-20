package communication;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public abstract class SocketConnectionManager extends Thread {
    private AsynchronousServerSocketChannel server;
    private ArrayList<ConnectionElement> connections = new ArrayList<>();
    private Future<AsynchronousSocketChannel> acceptCon;

    protected abstract void onOpen(ConnectionElement conn);
    protected abstract void onClose(ConnectionElement conn);
    protected abstract void onMessage(ConnectionElement conn, String message);

    // start a server on this device
    public SocketConnectionManager(int port) {
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
                    int result = 0;
                    try {
                        result = connection.result.get();
                        if (result < 0) { // if -1 bytes are read, it means the connection is closed
                            toRemove = connection;
                            break;
                        }
                    } catch (InterruptedException | ExecutionException e) {
                        connection.result.cancel(true);
                        toRemove = connection;
                    }

                    // Read the data from the buffer.
                    // The data always starts at index 0, and 'result' contains the amount of bytes.
                    String receivedMessage = new String(connection.buffer.array(), 0, result);

                    // append the received message to a possibly earlier received partial message
                    connection.partialMsg += receivedMessage;

                    // split the data on the first \r\n
                    String[] splittedData = connection.partialMsg.split("\r\n");

                    // if there was a \r\n we can send all data before it.
                    boolean lastLineEnded = connection.partialMsg.endsWith("\r\n");
                    int lineCount =  lastLineEnded ? splittedData.length : splittedData.length - 1;
                    for (int i = 0; i < lineCount; i++) {
                        this.onMessage(connection, splittedData[i].trim());
                    }

                    // the remained is kept for when there is more data.
                    if (!lastLineEnded) {
                        connection.partialMsg = splittedData[splittedData.length - 1].trim();
                    } else {
                        connection.partialMsg = "";
                    }
                    // set up the connection element for the next message
                    connection.buffer.clear();
                    connection.result = connection.channel.read(connection.buffer);
                }
            } else {
                connection.result.cancel(true);
                toRemove = connection;
            }
        }
        if (toRemove != null) {
            this.onClose(toRemove);
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

                    this.onOpen(elem);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

