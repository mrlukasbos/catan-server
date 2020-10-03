package game.phases;

import board.Dice;
import game.*;

public class SetupPhase implements GamePhase {
    Game game;
    Dice dice;

    public SetupPhase(Game game, Dice dice) {
        this.game = game;
        this.dice = dice;
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
    public Player determineFirstPlayer() {
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

