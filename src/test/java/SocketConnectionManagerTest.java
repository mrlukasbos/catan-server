import communication.ConnectionElement;
import communication.SocketConnectionManager;
import org.junit.jupiter.api.Test;

import java.io.DataOutputStream;
import java.net.Socket;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;



public class SocketConnectionManagerTest {

    @Test
    void itConnectsNewConnections() {
        SocketConnectionManagerStub socketConnectionManager = new SocketConnectionManagerStub(10006);
        socketConnectionManager.start();

        Socket s = null;
        DataOutputStream dout = null;
        try {
            s = new Socket("localhost", 10006);
            dout = new DataOutputStream(s.getOutputStream());
            dout.writeUTF("Hello\r\ngoodbye");
            dout.flush();
            dout.close();
            s.close();
            while (!s.isClosed()) {}
            System.out.println("closed this socket");
        } catch (Exception e) {
            System.out.println(e);
        }

        await().atMost(1, SECONDS).until(() -> socketConnectionManager.actions.equals("opened-Hello-goodbye-closed-"));
        assertEquals("opened-Hello-goodbye-closed-", socketConnectionManager.actions);
    }
}

class SocketConnectionManagerStub extends SocketConnectionManager {
    public String actions = "";

    SocketConnectionManagerStub(int port) {
        super(port);
    }

    @Override
    public void onOpen(ConnectionElement conn) {
        actions += "opened-";
    }

    @Override
    public void onClose(ConnectionElement conn) {
        actions += "closed-";
    }

    @Override
    public void onMessage(ConnectionElement conn, String message) {
        actions += (message + "-");
    }
}
