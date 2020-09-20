package communication;

import com.google.gson.JsonObject;
import game.GameManager;
import game.Player;
import game.Response;
import utils.Constants;
import utils.ValidationType;
import utils.jsonValidator;

import java.util.ArrayList;
import java.util.HashMap;

public class ServerUtils {
    ArrayList<Player> registeredConnections = new ArrayList<>();
    GameManager gameManager;

    public ServerUtils(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    public void handleMessage(Connection conn, String message) {
        // structure of the messages
        HashMap<String, ValidationType> props = new HashMap<>() {{
            put("model", ValidationType.STRING);
            put("attributes", ValidationType.OBJECT);
        }};
        JsonObject object = jsonValidator.getJsonObjectIfCorrect(message, props, null);
        if (object == null) return;

        String model = object.get("model").getAsString();
        JsonObject attrs = object.get("attributes").getAsJsonObject();

        switch(model) {
            case "join": joinPlayer(conn, attrs); break;
            case "leave": leavePlayer(conn, attrs); break;
            case "control": handleControl(conn, attrs); break;
            case "client-response": handleClientResponse(conn, attrs); break;
            default: break;
        }
    }

    public void handleConnect(Connection connection) {
     //   connection.send(gameManager.getCurrentGame().toString());
    }

    void handleDisconnect(Connection connection) {
        for (Player player : registeredConnections) {
            if (player.getConnection().equals(connection)) {
                print("lost connection with player: " + player.getName() + " using id: " + player.getId());
            }
        }
    }

    // receive a response of the client
    // the JSON validations will be done in the game phase,
    // since at this point we don't know what to expect yet.
    void handleClientResponse(Connection connection, JsonObject data) {
        HashMap<String, ValidationType> props = new HashMap<>() {{
            put("response", ValidationType.ARRAY);
        }};
        if (!jsonValidator.objectHasProperties(data, props, null)) return;

        for (Player player : registeredConnections) {
            if (connection.equals(player.getConnection())) {
                String buildRequest =  data.get("response").toString();
                player.setBufferedReply(buildRequest);
            }
        }
    }

    void leavePlayer(Connection connection, JsonObject data) {
        HashMap<String, ValidationType> props = new HashMap<>() {{
            put("id", ValidationType.NUMBER);
        }};
        if (!jsonValidator.objectHasProperties(data, props, null)) return;

        int connectionId = data.get("id").getAsInt();
        for (Player p : registeredConnections) {
            if (connectionId == p.getId()) {
                print("removing player: " + p.getName() + " using id: " + p.getId());
                gameManager.getCurrentGame().removePlayer(p);
                gameManager.getCurrentGame().signalGameChange();
            }
        }
    }

    void joinPlayer(Connection connection, JsonObject data) {
        HashMap<String, ValidationType> props = new HashMap<>() {{
            put("id", ValidationType.NUMBER);
            put("name", ValidationType.STRING);
        }};
        if (!jsonValidator.objectHasProperties(data, props, null)) return;
        int connectionId = data.get("id").getAsInt();

        boolean reconnection = false;
        Player player = null;
        for (Player p : registeredConnections) {
            if (connectionId == p.getId()) {

                print("reconnecting player: " + p.getName() + " using id: " + p.getId());
                p.setConnection(connection); // apparently a reconnect: renew the connection

                boolean participating = false;
                for (Player gamePlayer : gameManager.getCurrentGame().getPlayers()) {
                    if (p.getId() == gamePlayer.getId()) {
                        participating = true;
                        break;
                    }
                }
                // if the player was not joined in the game it must be added again
                if (!participating) {
                    gameManager.getCurrentGame().addPlayer(p);
                }

                reconnection = true;
                player = p;
                break;
            }
        }

        if (!reconnection || !registeredConnections.contains(player)) {
            registerPlayer(connection, data.get("name").getAsString());
        }
        gameManager.getCurrentGame().signalGameChange();
    }


    void print(String msg) {
        System.out.println("[communication.ServerUtils] \t \t" + msg);
    }

    private void handleControl(Connection connection, JsonObject data) {
        HashMap<String, ValidationType> props = new HashMap<>() {{
            put("command", ValidationType.STRING);
        }};
        if (!jsonValidator.objectHasProperties(data, props, null)) return;
        String command = data.get("command").getAsString();

        if (command.equals("START")) {
            print("Received START signal");
            if (gameManager.getCurrentGame().getPlayers().size() >= Constants.MINIMUM_AMOUNT_OF_PLAYERS && !gameManager.getCurrentGame().isRunning()) {
                gameManager.startGame();
            } else {
                print("not enough players to start with");
            }
        } else if (command.equals("STOP")) {
            print("Received STOP signal");
            gameManager.stopGame();
        } else {
            print(command);
        }
    }

    private void registerPlayer(Connection connection, String name) {
        Player newPlayer = new Player(connection, gameManager.getCurrentGame(), gameManager.getCurrentGame().getPlayers().size(), name);
        registeredConnections.add(newPlayer);

        if (!gameManager.getCurrentGame().isRunning()) {
            print("Registering new player: " + name);
            gameManager.getCurrentGame().addPlayer(newPlayer);
            Response idAcknowledgement = Constants.ID_ACK.withAdditionalInfo("" + newPlayer.getId());
            newPlayer.send(idAcknowledgement.toString());

        } else {
            print("warning! we must maybe reply with an error here. trying to register player while game is running");
        }
    }

    public ArrayList<Player> getRegisteredConnections() {
        return registeredConnections;
    }

    public GameManager getGameManager() {
        return gameManager;
    }
}
