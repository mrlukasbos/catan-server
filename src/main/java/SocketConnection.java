import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

public class SocketConnection extends Connection {
    ConnectionType type = ConnectionType.SOCKET;
    Socket socket;

    SocketConnection(Socket socket) {
        this.socket = socket;
    }

    boolean isOpen() {
        return !socket.isClosed();
    }

    void send(String message) {
        if (socket != null && !socket.isClosed()) {
            try {
                message += "\r\n";
                BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
                bos.write(message.getBytes("UTF-8"));
                bos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void close() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    Socket getSocket() {
        return socket;
    }
}
