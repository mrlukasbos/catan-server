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
//                while (connectionElement.writeResult != null && connectionElement.writeResult.isDone()) { Thread.onSpinWait(); }
                int amountOfBytesToWrite = message.getBytes(StandardCharsets.UTF_8).length;
                int bytesWritten = 0;

                while (bytesWritten < amountOfBytesToWrite) {
                    connectionElement.writeResult = connectionElement.channel.write(ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8)));
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
