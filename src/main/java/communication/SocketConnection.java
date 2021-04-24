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
                byte[] messageAsBytes = message.getBytes(StandardCharsets.UTF_8);
                int bytesWritten = 0;

                /*
                    Async sending does not necessarily write the entire buffer at once.
                    We use async sockets because we want to receive messages async.
                    But for sending it is preferred to block the process to prevent overlapping messages.
                    Therefore we only continue when we have confirmation that all bytes are written.
                 */
                while (bytesWritten < messageAsBytes.length) {
                    connectionElement.writeResult = connectionElement.channel.write(ByteBuffer.wrap(messageAsBytes));
                    bytesWritten += connectionElement.writeResult.get(); // block until the write operation is finished.
                    Thread.onSpinWait();
                }
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
