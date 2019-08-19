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

        Map<Resource, Integer> resourcesToRemove = new HashMap<>();
        Map<Resource, Integer> resourcesToAdd = new HashMap<>();

        for (JsonElement element : jsonArray) {
            JsonObject object = element.getAsJsonObject();

            Resource resourceFrom = Player.stringToResource(object.get("from").getAsString());
            Resource resourceTo = Player.stringToResource(object.get("to").getAsString());

            if (resourceFrom == Resource.NONE || resourceTo == Resource.NONE) {
                game.sendResponse(Constants.INVALID_TRADE_ERROR);
                return false;
            }

            if ((player.countResources(resourceFrom) - resourcesToRemove.getOrDefault(resourceFrom, 0)) + resourcesToAdd.getOrDefault(resourceFrom, 0) < Constants.MINIMUM_CARDS_FOR_TRADE) {
                game.sendResponse(Constants.NOTENOUGHRESOURCESERROR.withAdditionalInfo("required " + Constants.MINIMUM_CARDS_FOR_TRADE + " " + Player.resourceToString(resourceFrom) + " while you have " + player.countResources(resourceFrom)));
                return false;
            }

            resourcesToRemove.replace(resourceFrom, resourcesToRemove.getOrDefault(resourceFrom, 0) + Constants.MINIMUM_CARDS_FOR_TRADE);
            resourcesToAdd.replace(resourceTo, resourcesToAdd.getOrDefault(resourceTo, 0) + 1);
        }
        return true;
    }
}
