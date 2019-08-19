import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

public class TradePhase implements GamePhase {
    Game game;
    String txt = "Trade cards if you need to.";

    TradePhase(Game game) {
        this.game = game;
    }

    @Override
    public Phase getPhaseType() {
        return Phase.TRADING;
    }

    @Override
    public Phase execute() {
        if (game.getCurrentPlayer().canTradeWithBank()) {
            JsonArray jsonArray = getValidCommandFromUser(game.getCurrentPlayer());
            trade(game.getCurrentPlayer(), jsonArray);
            game.signalGameChange();
        } else {
            game.addEvent(new Event(game, EventType.GENERAL, game.getCurrentPlayer()).withGeneralMessage(" can't trade"));
        }
        return Phase.BUILDING;
    }

    private void trade(Player player, JsonArray jsonArray) {
        for (JsonElement element : jsonArray) {
            JsonObject object = element.getAsJsonObject();
            Resource resourceFrom = Player.stringToResource(object.get("from").getAsString());
            Resource resourceTo = Player.stringToResource(object.get("to").getAsString());

            player.removeResources(resourceFrom, Constants.MINIMUM_CARDS_FOR_TRADE);
            player.addResources(resourceTo, 1);

            HashMap<Resource, Integer> resourcesMap = new HashMap<>();
            resourcesMap.put(resourceFrom, Constants.MINIMUM_CARDS_FOR_TRADE);
            resourcesMap.put(resourceTo, 1);

            game.addEvent(new Event(game, EventType.TRADE, player).withResources(resourcesMap));
        }
    }


    // keep running this function until we get valid output from the user
    private JsonArray getValidCommandFromUser(Player currentPlayer) {
        currentPlayer.send(txt);
        boolean tradeSucceeded = false;
        JsonArray jsonArray = null;

        while (!tradeSucceeded) {
            String message = currentPlayer.listen();
            game.print("Received message from player " + currentPlayer.getName() + ": " + message);
            jsonArray = new jsonValidator().getJsonIfValid(currentPlayer, message);
            if (jsonArray == null) game.sendResponse(currentPlayer, Constants.MALFORMEDJSONERROR.withAdditionalInfo(message));
            tradeSucceeded = jsonArray != null && tradeIsValid(currentPlayer, jsonArray);

            if (!tradeSucceeded) {
                currentPlayer.send("try again! \n");
            }
        }
        return jsonArray;
    }


    private boolean tradeIsValid(Player player, JsonArray jsonArray) {

        // keep track of all the resources we need
        Map<Resource, Integer> resourcesNeeded = new HashMap<>();

        for (JsonElement element : jsonArray) {
            JsonObject object = element.getAsJsonObject();

            Resource resourceFrom = Player.stringToResource(object.get("from").getAsString());
            Resource resourceTo = Player.stringToResource(object.get("to").getAsString());

            if (resourceFrom == Resource.NONE || resourceTo == Resource.NONE) {
                game.sendResponse(Constants.INVALID_TRADE_ERROR);
                return false;
            }

            // add 4 to the corresponsing resource and subtract the resource we get (so we need one less)
            resourcesNeeded.put(resourceFrom, resourcesNeeded.getOrDefault(resourceFrom, 0) + Constants.MINIMUM_CARDS_FOR_TRADE);
            resourcesNeeded.put(resourceTo, resourcesNeeded.getOrDefault(resourceTo, 0) - 1);
        }

        for (Map.Entry<Resource, Integer> entry : resourcesNeeded.entrySet()) {
            int playerResourceCount = player.countResources(entry.getKey());
            if (playerResourceCount < entry.getValue()) {
                game.sendResponse(Constants.NOTENOUGHRESOURCESERROR.withAdditionalInfo(
                                "required " + entry.getValue() +
                                " " + Player.resourceToString(entry.getKey()) +
                                " while you have " + playerResourceCount));
                return false;
            }
        }
        return true;
    }
}
