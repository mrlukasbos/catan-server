import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

public class InterfaceServer extends WebSocketServer {
    private Game game;
    private SocketServer socketServer;

    // Create an interface for a specific port
    InterfaceServer(int port) {
        super(new InetSocketAddress(port));
        setReuseAddr(true);
    }

    // Start the interface thread
    void start(SocketServer socketServer, Game game) {
        this.game = game;
        this.socketServer = socketServer;
        start();
    }

    // When the visualization starts broadcasting show a message where we can connect to it
    @Override
    public void onStart() {
        try {
            print("Visualization started on:" + InetAddress.getLocalHost() + ":" + getPort());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        setConnectionLostTimeout(10);
    }

    // When a new connection is opened we have to transmit the game data to it
    @Override
    public void onOpen( WebSocket conn, ClientHandshake handshake ) {
        broadcast(game.toString());
    }

    // Receive messages from the interface
    // Currently we can receive start and stop signals from it to start/stop the game.
    @Override
    public void onMessage( WebSocket conn, String message ) {
        JsonParser parser = new JsonParser();
        JsonElement elem = parser.parse(message);
        JsonObject obj = elem.getAsJsonObject();

        String model = obj.get("model").getAsString();
        JsonObject attrs = obj.get("attributes").getAsJsonObject();

        switch(model) {
            case "join": {
                registerPlayer(conn, attrs.get("name").getAsString());
                break;
            }
            case "control": {
                handleControl(attrs.get("command").getAsString());
                break;
            }
            default: break;
        }
    }

    void handleControl(String command) {
        if (command.contains("START")) {
            print("Received START signal");

            print("THIS METHOD NEEDS TO BE CHANGED");
            if (socketServer.getConnections().size() >= Constants.MINIMUM_AMOUNT_OF_PLAYERS && !game.isRunning()) {
                game.startGame();
            } else {
                print("not enough players to start with");
            }
        } else if (command.contains("STOP")) {
            print("Received STOP signal");
            game.quit();
        } else {
            print(command);
        }
    }

    void registerPlayer(WebSocket conn, String name) {
        PlayerHuman newPlayer = new PlayerHuman(game, game.getPlayers().size(), name);
        newPlayer.setConnection(conn);
        print("Registering new interface player: " + name);
        game.addPlayer(newPlayer);
    }

    // Receive messages from the interface
    // This callback is currently not used
    @Override
    public void onMessage( WebSocket conn, ByteBuffer message ) {
        broadcast( message.array() );
        print(conn + ": " + message );
    }

    // When there is a problem with the connection print it
    @Override
    public void onError( WebSocket conn, Exception ex ) {
        ex.printStackTrace();
        if( conn != null ) {
            // some errors like port binding failed may not be assignable to a specific websocket
        }
    }

    // On close we don't have to do anything
    @Override
    public void onClose( WebSocket conn, int code, String reason, boolean remote ) { }

    private void print(String msg) {
        System.out.println("[Iface] \t" + msg);
    }
}
