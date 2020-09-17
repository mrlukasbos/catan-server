import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;

public class ServerUtils {
    ArrayList<Player> registeredConnections = new ArrayList<>();
    GameManager gameManager;

    ServerUtils(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    void handleMessage(Connection conn, String message) {
        JsonParser parser = new JsonParser();
        JsonElement elem = parser.parse(message);
        JsonObject obj = elem.getAsJsonObject();

        String model = obj.get("model").getAsString();

        switch(model) {
            case "join": {
                print("a player wants to join");
                JsonObject attrs = obj.get("attributes").getAsJsonObject();
                int connectionId = attrs.get("id").getAsInt();
                boolean reconnection = false;
                Player player = null;
                for (Player p : registeredConnections) {
                    if (connectionId == p.getId()) {

                        print("reconnecting player: " + p.getName() + " using id: " + p.getId());
                        p.setConnection(conn); // apparently a reconnect: renew the connection


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
                    registerPlayer(conn, attrs.get("name").getAsString());
                }
                gameManager.getCurrentGame().signalGameChange();
                break;

            }
            case "leave": {
                // todo use the new json validator validations here
                JsonObject attrs = obj.get("attributes").getAsJsonObject();
                int connectionId = attrs.get("id").getAsInt();
                for (Player p : registeredConnections) {
                    if (connectionId == p.getId()) {
                        print("removing player: " + p.getName() + " using id: " + p.getId());
                        gameManager.getCurrentGame().removePlayer(p);
                        gameManager.getCurrentGame().signalGameChange();
                    }
                }
                break;
            }
            case "control": {
                // todo use the new json validator validations here
                JsonObject attrs = obj.get("attributes").getAsJsonObject();
                handleControl(attrs.get("command").getAsString());
                break;
            }
            case "client-response": {
                for (Player player : registeredConnections) {
                    if (conn.equals(player.getConnection().getWebSocket())) {
                        // todo use the new json validator validations here
                        JsonArray arr = obj.get("attributes").getAsJsonArray();
                        String buildRequest = arr.toString();
                        player.setBufferedReply(buildRequest);
                    }
                }
            }
            default: break;
        }
    }

    void handleConnect(Connection connection) {
        connection.send(gameManager.getCurrentGame().toString());
    }

    void handleDisconnect(Connection connection) {
        for (Player player : registeredConnections) {
            if (player.getConnection() == connection) {
                print("lost connection with player: " + player.getName() + " using id: " + player.getId());
            }
        }
    }


    void print(String msg) {
        System.out.println("[ServerUtils] \t \t" + msg);
    }

    private void handleControl(String command) {
        if (command.contains("START")) {
            print("Received START signal");
            if (gameManager.getCurrentGame().getPlayers().size() >= Constants.MINIMUM_AMOUNT_OF_PLAYERS && !gameManager.getCurrentGame().isRunning()) {
                gameManager.startGame();
            } else {
                print("not enough players to start with");
            }
        } else if (command.contains("STOP")) {
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
            print("Registering new interface player: " + name);
            gameManager.getCurrentGame().addPlayer(newPlayer);
            Response idAcknowledgement = Constants.ID_ACK.withAdditionalInfo("" + newPlayer.getId());
            newPlayer.send(idAcknowledgement.toString());

        } else {
            print("warning! we must maybe reply with an error here");
        }
    }


}
