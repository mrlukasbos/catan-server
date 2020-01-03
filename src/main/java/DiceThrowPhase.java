import java.util.ArrayList;
import java.util.HashMap;

public class DiceThrowPhase implements GamePhase {
    Game game;
    Dice dice;

    DiceThrowPhase(Game game, Dice dice) {
        this.game = game;
        this.dice = dice;
    }

    public Phase getPhaseType() {
        return Phase.THROW_DICE;
    }

    public Phase execute() {
        int diceValue = dice.throwDice();
        game.setLastDiceThrow(diceValue);
        game.addEvent(new Event(game, EventType.GENERAL, game.getCurrentPlayer()).withGeneralMessage(" has thrown " + diceValue));

        if (diceValue == 7) {
            game.signalGameChange();
            return Phase.FORCE_DISCARD;
        } else {
            distributeResourcesForDice(diceValue);

            game.signalGameChange();
            return Phase.TRADING;
        }
    }

    // Give all the resources to the players for a given dicethrow
    private void distributeResourcesForDice(int diceThrow) {

        // create a hashmap for each player (for the logging)
        ArrayList<HashMap<Resource, Integer>> resourcesForPlayerId = new ArrayList<>();
        for (Player player : game.getPlayers()) {
            resourcesForPlayerId.add(player.getId(), new HashMap<>());
        }

        for (Tile tile : game.getBoard().getTilesForDiceNumber(diceThrow)) {
            if (game.getBoard().getBandit().getTile() == tile) continue;
            for (Node node : game.getBoard().getNodes(tile)) {
                // TODO This needs some looking into, currently each tile returns 18 nodes. These are obviously duplicates created during board.init()
//                game.print("nodes for tile " + game.getBoard().getNodes(tile).size());
                if (node.hasPlayer() && node.hasStructure()) {
                    int amount = node.getStructure() == Structure.CITY ? 2 : 1;
                    Resource resource = tile.produces();

                    node.getPlayer().addResources(resource, amount);
                    resourcesForPlayerId.get(node.getPlayer().getId()).put(resource, amount);
                }
            }
        }

        // put hashmaps to the game events
        for (Player player : game.getPlayers()) {
            game.addEvent(new Event(game, EventType.GET_RESOURCES, player).withResources(resourcesForPlayerId.get(player.getId())));
        }
    }
}

