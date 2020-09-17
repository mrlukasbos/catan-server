import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashMap;

public class MoveBanditPhase implements GamePhase {
    Game game;
    Response request = Constants.MOVE_BANDIT_REQUEST;
    // we expect a location with a string value as inputs
    HashMap<String, ValidationType> props = new HashMap<>() {{
        put("location", ValidationType.TILE_KEYS);
    }};

    MoveBanditPhase(Game game) {
        this.game = game;
    }

    public Phase getPhaseType() {
        return Phase.MOVE_BANDIT;
    }

    public Phase execute() {
        JsonArray jsonArray = getValidCommandFromUser(game.getCurrentPlayer());
        move(game.getCurrentPlayer(), jsonArray);

        game.addEvent(new Event(game, EventType.BANDIT_PLACED, game.getCurrentPlayer()).withGeneralMessage(" placed the bandit"));

        game.signalGameChange();
        return Phase.TRADING;
    }

    void move(Player player, JsonArray jsonArray) {
        JsonObject jsonObject = jsonArray.get(0).getAsJsonObject();
        JsonElement locationElement = jsonObject.get("location");
        Tile tile = game.getBoard().getTile(locationElement.getAsString());
        game.getBoard().getBandit().setTile(tile);
        game.sendResponse(player, Constants.OK.withAdditionalInfo("Bandit move processed succesfully!"));
    }

    // keep running this function until we get valid output from the user
    JsonArray getValidCommandFromUser(Player currentPlayer) {
        currentPlayer.send(request.toString());
        boolean moveSucceeded = false;
        JsonArray jsonArray = null;

        while (game.isRunning() && !moveSucceeded) {
            String message = currentPlayer.listen();
            game.print("Received message from player " + currentPlayer.getName() + ": " + message);
            jsonArray = getJsonIfValid(message);
            if (jsonArray == null) game.sendResponse(currentPlayer, Constants.MALFORMED_JSON_ERROR.withAdditionalInfo(message));
            moveSucceeded = jsonArray != null && moveIsValid(jsonArray);

            if (!moveSucceeded) {
                currentPlayer.send(request.toString());
            }
        }
        return jsonArray;
    }

    JsonArray getJsonIfValid(String message) {
        return jsonValidator.getJsonArrayIfCorrect(message, props, game.getBoard());
    }

    boolean moveIsValid(JsonArray jsonArray) {
        if (jsonArray.size() == 0) return false;
        JsonObject jsonObject = jsonArray.get(0).getAsJsonObject();
        JsonElement locationElement = jsonObject.get("location");
        Tile tile = game.getBoard().getTile(locationElement.getAsString());

        if (game.getBoard().getBandit().getTile() == tile) {
            game.sendResponse(Constants.CAN_NOT_PLACE_BANDIT_ON_SAME_TILE_ERROR);
            return false;
        }
        if (!tile.isTerrain()) {
            game.sendResponse(Constants.CAN_NOT_PLACE_BANDIT_ON_SEA_TILE_ERROR);
            return false;
        }
        return true;
    }
}

