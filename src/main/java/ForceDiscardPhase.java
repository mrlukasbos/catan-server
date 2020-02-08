import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.HashMap;

public class ForceDiscardPhase implements GamePhase {
    Game game;
    Response request = Constants.DISCARD_RESOURCES_REQUEST;

    ForceDiscardPhase(Game game) {
        this.game = game;
    }

    public Phase getPhaseType() {
        return Phase.FORCE_DISCARD;
    }

    public Phase execute() {
        for (Player player: game.getPlayers()) {
            if (player.countResources() <= 7) continue;

            JsonArray jsonArray = getValidCommandFromUser(player);
            discard(player, jsonArray);

            game.addEvent(new Event(game, EventType.CARDS_DISCARDED, player).withGeneralMessage(" discarded their cards"));
        }

        game.signalGameChange();
        return Phase.MOVE_BANDIT;
    }

    void discard(Player player, JsonArray jsonArray) {
        JsonObject jsonObject = jsonArray.get(0).getAsJsonObject();

        for (Resource resource : Constants.ALL_RESOURCES) {
            if (jsonObject.has(resource.name().toLowerCase())) {
                player.removeResources(resource, jsonObject.get(resource.name().toLowerCase()).getAsInt());
            }
        }

        game.sendResponse(player, Constants.OK.withAdditionalInfo("Discard processed succesfully!"));
    }

    // keep running this function until we get valid output from the user
    JsonArray getValidCommandFromUser(Player player) {
        player.send(request.withAdditionalInfo(Double.toString(Math.floor(player.countResources()/2))).toString());
        boolean discardSucceeded = false;
        JsonArray jsonArray = null;

        while (!discardSucceeded) {
            String message = player.listen();
            game.print("Received message from player " + player.getName() + ": " + message);
            jsonArray = new jsonValidator().getJsonIfValid(player, message);
            if (jsonArray == null) game.sendResponse(player, Constants.MALFORMED_JSON_ERROR.withAdditionalInfo(message));
            discardSucceeded = jsonArray != null && discardIsValid(player, jsonArray);

            if (!discardSucceeded) {
                player.send(request.withAdditionalInfo(Double.toString(Math.floor(player.countResources()/2))).toString());
            }
        }
        return jsonArray;
    }

    boolean discardIsValid(Player player, JsonArray jsonArray) {
        if (jsonArray.size() == 0) return false;

        JsonObject jsonObject = jsonArray.get(0).getAsJsonObject();

        int totalDiscarded = 0;
        for (Resource resource : Constants.ALL_RESOURCES) {
            int amount = 0;
            if (jsonObject.has(resource.name().toLowerCase())) {
                amount = jsonObject.get(resource.name().toLowerCase()).getAsInt();
                totalDiscarded += amount;
            }

            if (player.countResources(resource) < amount) {
                game.sendResponse(player, Constants.MORE_RESOURCES_DISCARDED_THAN_OWNED_ERROR.withAdditionalInfo("you tried to discard " + amount + " " + resource.name().toLowerCase() + " but only have " + player.countResources(resource)));
                return false;
            }
        }

        int amountToDiscard = (int) Math.floor(player.countResources()/2);
        if (totalDiscarded < amountToDiscard) {
            game.sendResponse(player, Constants.NOT_ENOUGH_RESOURCES_DISCARDED_ERROR.withAdditionalInfo("you only discarded " + totalDiscarded + " of the " + amountToDiscard + " resources you need to discard"));
            return false;
        }

        return true;
    }
}

