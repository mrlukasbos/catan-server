import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

public class TradePhase implements GamePhase {
    Game game;
    Response request = Constants.TRADE_REQUEST;

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

    // the validation has to succeed before you call this function
    void trade(Player player, JsonArray jsonArray) {
        for (JsonElement element : jsonArray) {
            JsonObject object = element.getAsJsonObject();

            Resource resourceFrom;
            Resource resourceTo;

            resourceFrom = Enum.valueOf(Resource.class, object.get("from").getAsString().toUpperCase());
            resourceTo = Enum.valueOf(Resource.class, object.get("to").getAsString().toUpperCase());

            int requiredResourcesForBankTrade = getRequiredAmountOfCardsToTrade(player, resourceFrom);

            player.removeResources(resourceFrom, requiredResourcesForBankTrade);
            player.addResources(resourceTo, 1);

            HashMap<Resource, Integer> resourcesMap = new HashMap<>();
            resourcesMap.put(resourceFrom, requiredResourcesForBankTrade);
            resourcesMap.put(resourceTo, 1);

            game.addEvent(new Event(game, EventType.TRADE, player).withResources(resourcesMap));
        }
    }


    // keep running this function until we get valid output from the user
    private JsonArray getValidCommandFromUser(Player currentPlayer) {
        currentPlayer.send(request.toString());
        boolean tradeSucceeded = false;
        JsonArray jsonArray = null;

        while (!tradeSucceeded) {
            String message = currentPlayer.listen();
            game.print("Received message from player " + currentPlayer.getName() + ": " + message);
            jsonArray = new jsonValidator().getJsonIfValid(currentPlayer, message);
            if (jsonArray == null) game.sendResponse(currentPlayer, Constants.MALFORMED_JSON_ERROR.withAdditionalInfo(message));
            tradeSucceeded = jsonArray != null && tradeIsValid(currentPlayer, jsonArray);

            if (!tradeSucceeded) {
                currentPlayer.send(request.toString());
            }
        }
        return jsonArray;
    }


    boolean tradeIsValid(Player player, JsonArray jsonArray) {

        // keep track of all the resources we need
        Map<Resource, Integer> resourcesNeeded = new HashMap<>();

        for (JsonElement element : jsonArray) {
            JsonObject object = element.getAsJsonObject();

            JsonElement fromElement = object.get("from");
            JsonElement toElement = object.get("to");
            if (fromElement == null || toElement == null) {
                game.sendResponse(Constants.INVALID_TRADE_ERROR);
                return false;
            }

            Resource resourceFrom;
            Resource resourceTo;
            try {
                resourceFrom = Enum.valueOf(Resource.class, fromElement.getAsString().toUpperCase());
                resourceTo = Enum.valueOf(Resource.class, toElement.getAsString().toUpperCase());
            } catch (IllegalArgumentException e) {
                game.sendResponse(Constants.INVALID_TRADE_ERROR);
                return false;
            }

            int requiredResourcesForBankTrade = getRequiredAmountOfCardsToTrade(player, resourceFrom);

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

    int getRequiredAmountOfCardsToTrade(Player player, Resource resourceFrom) {
        int requiredResourcesForBankTrade =  Constants.MINIMUM_CARDS_FOR_TRADE; // default (harbours change this)
        if (game.getBoard().playerHasHarbour(player, HarbourType.HARBOUR_ALL)) {
            requiredResourcesForBankTrade = 3;
        } else if (game.getBoard().playerHasHarbour(player)) {
            if (game.getBoard().playerHasHarbour(player, Constants.RESOURCES_HARBOURS.get(resourceFrom))) {
                requiredResourcesForBankTrade = 2;
            }
        }
        return requiredResourcesForBankTrade;
    }
}
