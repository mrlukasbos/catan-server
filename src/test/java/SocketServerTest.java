import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;

public class SocketServerTest {
    SocketConnectionServer socketServer = new SocketConnectionServer(10006);
    WebSocketConnectionServer websocketServer = new WebSocketConnectionServer( 10007);
    GameManager gameManager = new GameManager(socketServer, websocketServer);

    @Test
    void itConnectsNewConnections() throws InterruptedException, IOException {
        socketServer.start(gameManager);
        assertEquals(socketServer.getConnections().size(), 0);

        Socket s = null;
        DataOutputStream dout = null;
        try {
            s = new Socket("localhost", 10006);
            dout = new DataOutputStream(s.getOutputStream());
            dout.writeUTF("Hello");
            dout.flush();
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

        i = 0;
        while (socketServer.getConnectedPlayers().size() != 1) {
            Thread.sleep(50);
            i++;
            if (i > 20) fail();
        }
       // assertEquals("Hello", socketServer.getConnectedPlayers().get(0).getName());

        assert s != null;
        assert dout != null;

        BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
        assertNotEquals("", in.readLine());

        dout.close();
        s.close();


        // give the server a second to process and respond
        i = 0;
        while (socketServer.getConnections().size() != 0) {
            Thread.sleep(50);
            i++;
            if (i > 20) fail();
        }
    }
}
