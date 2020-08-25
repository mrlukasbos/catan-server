import org.java_websocket.WebSocket;

public class PlayerHuman extends Player {
    WebSocket connection;
    String bufferedReply;

    synchronized void setBufferedReply(String bufferedReply) {
        this.bufferedReply = bufferedReply;
    }

    @Override
    void send(String str) {
        connection.send(str);
    }

    @Override
    void stop() {
        connection.close();
    }

    @Override
    String listen() {
        while (bufferedReply.equals("")) {}  // block until there is a reply

        String reply = bufferedReply;
        bufferedReply = "";
        return reply;
    }

    PlayerHuman(Game game, int id, String name) {
        super(game, id, name);
    }
    void setConnection(WebSocket connection) {
        this.connection = connection;
    }

    public WebSocket getConnection() {
        return connection;
    }
}
