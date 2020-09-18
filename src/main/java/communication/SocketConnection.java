package communication;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.StandardCharsets;

public class SocketConnection extends Connection {
    ConnectionElement connectionElement;

    SocketConnection(ConnectionElement connectionElement) {
        this.connectionElement = connectionElement;
    }

    public boolean isOpen() {
        return connectionElement.channel.isOpen();
    }

    public void send(String message) {
        // set up the connection element for the next message
        if (connectionElement.channel != null && connectionElement.channel.isOpen() && !message.equals("")) {
            message += "\r\n";
            try {
                // wait until we can write
                while (connectionElement.isReading) { }
                connectionElement.isWriting = true;
                connectionElement.channel.write(ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8)));
                connectionElement.isWriting = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void close() {
        try {
            connectionElement.channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public AsynchronousSocketChannel getSocket() {
        return connectionElement.channel;
    }
}
