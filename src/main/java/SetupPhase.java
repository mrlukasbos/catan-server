public class SetupPhase implements GamePhase {
    Game game;

    SetupPhase(Game game){
        this.game = game;
    }

    public Phase getPhaseType() {
        return Phase.SETUP;
    }

    public Phase execute() {
        game.setCurrentPlayer(determineFirstPlayer());
        return Phase.INITIAL_BUILDING;
    }

    // throw the dice, the highest throw can start
    private Player determineFirstPlayer() {
        Dice dice = new Dice(1);
        int highestDiceThrow = 0;
        Player firstPlayer = game.getPlayers().get(0);
        for (Player player : game.getPlayers()) {
            int newDiceThrow = dice.throwDice();
            if (newDiceThrow > highestDiceThrow) {
                firstPlayer = player;
                highestDiceThrow = newDiceThrow;
            }
        }
        return firstPlayer;
    }
}

