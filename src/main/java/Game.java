import java.util.ArrayList;

class Game {
    private int dice = 0;
    private Board board;
    private ArrayList<Player> players = new ArrayList<Player>();
    private int currentPlayerId = 0;

    Game(int amountOfPlayers) {
        board = new Board();
        for (int i = 0; i < amountOfPlayers; i++) {
            players.add(new Player(i, "player " + i));
        }
    }

    void start() {

    }

    // throw the dice, the highest throw can start
    Player determineFirstPlayer() {
        int highestDiceThrow = 0;
        Player firstPlayer = players.get(0);
        for (Player player : players) {
            int newDiceThrow = player.throwDice();
            if (newDiceThrow > highestDiceThrow) {
                firstPlayer = player;
                highestDiceThrow = newDiceThrow;
            }
        }
        currentPlayerId = firstPlayer.getId();
        return firstPlayer;
    }

    // move the currentPlayer id to the next Player in the array.
    Player progressToNextPlayer() {
        Player nextPlayer = players.get((currentPlayerId + 1) % players.size());
        currentPlayerId = nextPlayer.getId();
        return nextPlayer;
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public Board getBoard() {
        return board;
    }
}

enum Phase {
    SETUP,
    THROW_DICE,
    MOVE_BARBARIAN,
    GATHER_RESOURCES,
    BUILDING,
    PLAYING_CARD
}