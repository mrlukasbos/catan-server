package communication;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.StandardCharsets;

public class SocketConnection extends Connection {
    AsynchronousSocketChannel socket;

    SocketConnection(AsynchronousSocketChannel socket) {
        this.socket = socket;
    }

    public boolean isOpen() {
        return socket.isOpen();
    }

    public void send(String message) {
        if (socket != null && socket.isOpen() && !message.equals("")) {
            message += "\r\n";
            try {
                socket.write(ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public AsynchronousSocketChannel getSocket() {
        return socket;
    }
}
