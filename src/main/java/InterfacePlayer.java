import org.java_websocket.WebSocket;

public class InterfacePlayer extends Player {
    WebSocket connection;
    String bufferedReply = "";

    synchronized void setBufferedReply(String bufferedReply) {
        this.bufferedReply = bufferedReply;
        notify();
    }

    @Override
    void send(String str) {
        if (connection.isOpen()) {
            connection.send(str);
        } else {
            System.out.println("cannot send to player: " + getName() + ", the socket is closed");
        }
    }

    @Override
    void stop() {
        connection.close();
    }

    @Override
    String listen() {
        while (bufferedReply.equals("")) {
            try {
                wait();
            } catch (Exception e) {}
        }  // block until there is a reply

        String reply = bufferedReply;
        bufferedReply = "";
        return reply;
    }

    InterfacePlayer(Game game, int id, String name) {
        super(game, id, name);
    }
    void setConnection(WebSocket connection) {
        this.connection = connection;
    }

    public WebSocket getConnection() {
        return connection;
    }
}
