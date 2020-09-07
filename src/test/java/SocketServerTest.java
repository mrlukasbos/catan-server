import org.junit.jupiter.api.Test;

import java.io.DataOutputStream;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;

public class SocketServerTest {
    SocketConnectionServer socketServer = new SocketConnectionServer(10006);
    WebSocketConnectionServer websocketServer = new WebSocketConnectionServer( 10007);
    GameManager gameManager = new GameManager(socketServer, websocketServer);

    @Test
    void itConnectsNewConnections() throws InterruptedException {
        socketServer.start(gameManager);
        assertEquals(socketServer.getConnections().size(), 0);

        try {
            Socket s = new Socket("localhost", 10006);
            DataOutputStream dout = new DataOutputStream(s.getOutputStream());
            dout.writeUTF("Hello");
            dout.flush();
            dout.close();
            s.close();
        } catch (Exception e) {
            System.out.println(e);
        }

        // give the server a second to process and respond
        int i = 0;
        while (socketServer.getConnections().size() != 1) {
            Thread.sleep(50);
            i++;
            if (i > 20) fail();
        }
    }
}
