public class DiceThrowPhase implements GamePhase {
    Game game;

    DiceThrowPhase(Game game) {
        this.game = game;
    }

    public Phase execute() {
        Dice d = new Dice(2);
        int dice = d.throwDice();
        game.setLastDiceThrow(dice);
        if (dice == 7) {
            return Phase.MOVE_BANDIT;
        } else {
            distributeResourcesForDice(dice);
            return Phase.BUILDING;
        }
    }

    private void distributeResourcesForDice(int diceThrow) {
        for (Node node : game.getBoard().getNodes()) {
            if (node.hasPlayer() && node.hasStructure()) {
                for (Tile tile : node.getTiles()) {
                    if (tile.isTerrain() && tile.getNumber() == diceThrow) {
                        int amount = node.getStructure() == Structure.CITY ? 2 : 1;
                        node.getPlayer().addResources(tile.typeToResource(tile.getType()), amount);
                    }
                }
            }
        }
    }
}

