/*
All game mechanics
 */

import java.util.ArrayList;

class Game extends Thread {

    private int lastDiceThrow = 0;
    private Board board;
    private ArrayList<Player> players = new ArrayList<Player>();
    private Player currentPlayer;
    private boolean running = false;
    private Interface iface;
    private Server server;

    // All gamePhases
    private DiceThrowPhase diceThrowPhase = new DiceThrowPhase(this);
    private SetupPhase setupPhase = new SetupPhase(this);
    private InitialBuildPhase initialBuildPhase = new InitialBuildPhase(this);
    private BuildPhase normalBuildPhase = new BuildPhase(this);
    private GamePhase currentPhase = new SetupPhase(this);

    Game(Interface iface, Server server) {
        this.iface = iface;
        this.server = server;
    }

    synchronized void startGame() {
        this.board = new Board(this);
        this.running = true;
        print("Starting game");
        start();
    }

    public void run() {
        while (true) {
            if (isRunning()) {
                Phase nextPhase = currentPhase.execute();

                print("Going to phase: " + phaseToString(nextPhase));
                currentPhase = getGamePhase(nextPhase);

                signalGameChange();
            }
        }
    }

    private void signalGameChange() {
        // signal the change
        getPlayers().forEach((p) -> p.send(getBoard().toString()));
        iface.broadcast(toString());
    }

    Structure stringToStructure(String str) {
        switch (str) {
            case "village":
                return Structure.SETTLEMENT;
            case "city":
                return Structure.CITY;
            case "street":
                return Structure.STREET;
            default:
                return Structure.NONE;
        }
    }

    void quit() {
        this.board = null;
        this.players = null;
        this.running = false;
        print("Stopping game");

        try {
            join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private GamePhase getGamePhase(Phase phase) {
        switch (phase) {
            case SETUP:
                return setupPhase;
            case THROW_DICE:
                return diceThrowPhase;
            case BUILDING:
                return normalBuildPhase;
            case INITIAL_BUILDING:
                return initialBuildPhase;
            default:
                return setupPhase;
        }
    }


    private String phaseToString(Phase phase) {
        switch (phase) {
            case SETUP: return "SETUP";
            case THROW_DICE: return "THROW_DICE";
            case FORCE_DISCARD: return "FORCE_DISCARD";
            case MOVE_BANDIT: return "MOVE_BANDIT";
            case BUILDING: return "BUILDING";
            case INITIAL_BUILDING: return "INITIAL_BUILDING";
            default: return "Unknown";
        }
    }

    @Override
    public String toString() {

        String playersString = "[";
        if (getPlayers().size() > 0) {
            for (Player player : getPlayers()) {
                playersString = playersString.concat(player.toString() + ",");
            }
            playersString = playersString.substring(0, playersString.length() - 1);
            playersString = playersString.concat("]");
        } else {
            playersString = "[]";
        }

        if (isRunning()) {

            return "{" +
                    "\"model\": \"game\", " +
                    "\"attributes\": {" +
                    "\"players\": " + playersString + ", " +
                    "\"status\": \"" + getGameStatus() + "\", " +
                    "\"board\": " + getBoard().toString() + ", " +
                    "\"lastDiceThrow\": " + getLastDiceThrow() + ", " +
                    "\"phase\": \"" + phaseToString(currentPhase.getPhaseType()) + "\"," +
                    "\"currentPlayer\": " + getCurrentPlayer().getId() +
                    "}" +
                    '}';
        } else {
            return "{" +
                    "\"model\": \"game\", " +
                    "\"attributes\": {" +
                    "\"players\": " + playersString + ", " +
                    "\"status\": \"" + getGameStatus() + "\"" +
                    "}" +
                    '}';
        }
    }


    String getGameStatus() {
        if (getPlayers().size() < Constants.MINIMUM_AMOUNT_OF_PLAYERS) {
            return "WAITING_FOR_PLAYERS";
        } else if (!isRunning()) {
            return "WAITING_FOR_TAKEOFF";
        } else {
            return "GAME_RUNNING";
        }
    }

    void addPlayer (Player p) {
        players.add(p);
    }

    ArrayList<Player> getPlayers() { return players; }

    Board getBoard() { return board; }

    boolean isRunning() { return running; }

    void print(String msg) {
        System.out.println("[Game] \t \t" + msg);
    }

    void setCurrentPlayer(Player player) { currentPlayer = player; }

    Player getCurrentPlayer() { return currentPlayer; }

    int getLastDiceThrow() { return lastDiceThrow; }

    void setLastDiceThrow(int diceThrow) { lastDiceThrow = diceThrow; }

    void goToNextPlayer() {
      setCurrentPlayer(getNextPlayer());
    }

    // move the currentPlayer id to the next Player in the array.
    private Player getNextPlayer() {
        return getPlayers().get((getCurrentPlayer().getId() + 1) % getPlayers().size());
    }

}

enum Phase {
    SETUP,
    INITIAL_BUILDING,
    END_TURN,
    THROW_DICE,
    FORCE_DISCARD,
    MOVE_BANDIT,
    BUILDING
}
