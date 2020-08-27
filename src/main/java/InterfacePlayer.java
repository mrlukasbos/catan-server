import org.java_websocket.WebSocket;

public class InterfacePlayer extends Player {
    WebSocket connection;

    @Override
    void send(String str) {
        connection.send(str);
    }

    @Override
    void stop() {
        connection.close();
    }

    @Override
    String listen() { return null; };

    InterfacePlayer(Game game, int id, String name) {
        super(game, id, name);
    }
    void setConnection(WebSocket connection) {
        this.connection = connection;
    }
}
