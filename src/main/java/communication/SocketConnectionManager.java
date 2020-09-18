package communication;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.StandardCharsets;
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

                    // execute the messages line by line


                    byte[] bytes = connection.buffer.array();
                    String receivedMessage = new String(bytes, 0, result);

                    System.out.println(" ------ full received message: " + receivedMessage);



                    connection.partialMsg += receivedMessage;
                    System.out.println("new partial msg: " + connection.partialMsg);

                    String[] splittedData = connection.partialMsg.split("\r\n", 2); // split the data on the first \r\n

                    if (splittedData.length > 1) {
                        // got a full line. lets propagate it
                        this.onMessage(connection, splittedData[0].trim());
                        connection.partialMsg = splittedData[1].trim();
                        System.out.println("----- continueing with: " + connection.partialMsg);
                    } else {
                        System.out.println("----- we did not receive a complete message, so we wait until more data arrives " + connection.partialMsg);

                    }


                    // set up the connection element for the next message

//                    if (!connection.isWriting) {
//                        connection.isReading = true;
                    connection.buffer = ByteBuffer.allocate(2048);
                    connection.result = connection.channel.read(connection.buffer);
//                        connection.isReading = false;
//                    }
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

