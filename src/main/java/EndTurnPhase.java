public class EndTurnPhase implements GamePhase{
    Game game;

    EndTurnPhase(Game game){
        this.game = game;
    }

    public Phase execute() {
        game.setCurrentPlayer(getNextPlayer());
        return Phase.THROW_DICE;
    }

    public Phase getPhaseType() {
        return Phase.END_TURN;
    }

    // move the currentPlayer id to the next Player in the array.
    private Player getNextPlayer() {
        return game.getPlayers().get((game.getCurrentPlayer().getId() + 1) % game.getPlayers().size());
    }
}

