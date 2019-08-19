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
        game.addEvent(new Event(game, EventType.GENERAL, game.getCurrentPlayer()).withGeneralMessage(" may start the game"));

        game.signalGameChange();
        return Phase.INITIAL_BUILDING;
    }

    // throw the dice, the highest throw can start
    private Player determineFirstPlayer() {
        Dice dice = new Dice(1);
        int highestDiceThrow = 0;
        Player firstPlayer = game.getPlayers().get(0);

        for (Player player : game.getPlayers()) {
            int newDiceThrow = dice.throwDice();
            game.addEvent(new Event(game, EventType.GENERAL, player).withGeneralMessage(" throws " + newDiceThrow));
            if (newDiceThrow > highestDiceThrow) {
                firstPlayer = player;
                highestDiceThrow = newDiceThrow;
            }
        }
        return firstPlayer;
    }
}

