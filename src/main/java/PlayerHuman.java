import org.java_websocket.WebSocket;

public class PlayerHuman extends Player {
    WebSocket connection;

    @Override
    void send(String str) {
        connection.send(str);
    }

    @Override
    String listen() { return null; };

    PlayerHuman(Game game, int id, String name) {
        super(game, id, name);
    }
    void setConnection(WebSocket connection) {
        this.connection = connection;
    }
}
