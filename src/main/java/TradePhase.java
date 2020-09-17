import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

public class TradePhase implements GamePhase {
    Game game;
    Response request = Constants.TRADE_REQUEST;
    HashMap<String, ValidationType> props = new HashMap<>() {{
        put("from", ValidationType.RESOURCE);
        put("to", ValidationType.RESOURCE);
    }};

    TradePhase(Game game) {
        this.game = game;
    }

    @Override
    public Phase getPhaseType() {
        return Phase.TRADING;
    }

    @Override
    public Phase execute() {
        Player player = game.getCurrentPlayer();
        if (player.canTradeWithBank()) {
            JsonArray jsonArray = getValidCommandFromUser(player);
            trade(player, jsonArray);
            game.signalGameChange();
        } else {
            game.addEvent(new Event(game, EventType.GENERAL, player).withGeneralMessage(" can't trade"));
        }
        return Phase.BUILDING;
    }

    // the validation has to succeed before you call this function
    void trade(Player player, JsonArray jsonArray) {
        for (JsonElement element : jsonArray) {
            JsonObject object = element.getAsJsonObject();
            Resource resourceFrom = Helpers.getResourceByName(object.get("from").getAsString());
            Resource resourceTo = Helpers.getResourceByName(object.get("to").getAsString());

            int requiredResourcesForBankTrade = game.getRequiredAmountOfCardsToTrade(player, resourceFrom);

            player.removeResources(resourceFrom, requiredResourcesForBankTrade);
            player.addResources(resourceTo, 1);

            HashMap<Resource, Integer> resourcesMap = new HashMap<>();
            resourcesMap.put(resourceFrom, requiredResourcesForBankTrade);
            resourcesMap.put(resourceTo, 1);

            game.addEvent(new Event(game, EventType.TRADE, player).withResources(resourcesMap));
        }
        game.sendResponse(Constants.OK.withAdditionalInfo("Trade was successful!"));
    }


    // keep running this function until we get valid output from the user
    JsonArray getValidCommandFromUser(Player currentPlayer) {
        currentPlayer.send(request.toString());
        boolean tradeSucceeded = false;
        JsonArray jsonArray = null;

        while (!tradeSucceeded) {
            String message = currentPlayer.listen();
            game.print("Received message from player " + currentPlayer.getName() + ": " + message);
            jsonArray = getValidJson(message);
            if (jsonArray == null) game.sendResponse(currentPlayer, Constants.MALFORMED_JSON_ERROR.withAdditionalInfo(message));
            tradeSucceeded = jsonArray != null && tradeIsValid(currentPlayer, jsonArray);

            if (!tradeSucceeded) {
                currentPlayer.send(request.toString());
            }
        }
        return jsonArray;
    }

    JsonArray getValidJson(String message) {
        return jsonValidator.getJsonArrayIfCorrect(message, props, game.getBoard());
    }

    boolean tradeIsValid(Player player, JsonArray jsonArray) {
        // keep track of all the resources we need
        Map<Resource, Integer> resourcesNeeded = new HashMap<>();

        for (JsonElement element : jsonArray) {
            JsonObject object = element.getAsJsonObject();
            Resource resourceFrom = Helpers.getResourceByName(object.get("from").getAsString());
            Resource resourceTo = Helpers.getResourceByName(object.get("to").getAsString());

            int requiredResourcesForBankTrade = game.getRequiredAmountOfCardsToTrade(player, resourceFrom);

            // add 4 to the corresponding resource and subtract the resource we get (so we need one less)
            resourcesNeeded.put(resourceFrom, resourcesNeeded.getOrDefault(resourceFrom, 0) + requiredResourcesForBankTrade);
            resourcesNeeded.put(resourceTo, resourcesNeeded.getOrDefault(resourceTo, 0) - 1);
        }

        for (Map.Entry<Resource, Integer> entry : resourcesNeeded.entrySet()) {
            int playerResourceCount = player.countResources(entry.getKey());
            if (playerResourceCount < entry.getValue()) {
                game.sendResponse(Constants.INSUFFICIENT_RESOURCES_ERROR.withAdditionalInfo(
                                "required " + entry.getValue() +
                                " " + entry.getKey().toString() +
                                " while you have " + playerResourceCount));
                return false;
            }
        }
        return true;
    }
}
