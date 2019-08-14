/*
All game mechanics
 */

import java.util.ArrayList;

class Game extends Thread {
    private int lastDiceThrow = 0;
    private Board board;
    private ArrayList<Player> players;
    private Player currentPlayer;
    private boolean running = false;
    private Interface iface;

    // all gamePhases
    private DiceThrowPhase diceThrowPhase = new DiceThrowPhase(this);
    private SetupPhase setupPhase = new SetupPhase(this);
    private BuildPhase buildPhase = new BuildPhase(this);
    private EndTurnPhase endTurnPhase = new EndTurnPhase(this);

    Game(Interface iface) {
        this.iface = iface;
    }

    synchronized void start(ArrayList<Player> players) {
        this.board = new Board(this);
        this.players = new ArrayList<Player>(players); // this copies the arraylist
        this.running = true;
        print("Starting game");
        start();
    }

    public void run() {
        GamePhase currentPhase = new SetupPhase(this);
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
        iface.broadcast(broadcastType.BOARD, getBoard().toString());
        iface.broadcastStatus();
        iface.broadcastPlayerInfo();
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
                return buildPhase;
            case END_TURN:
                return endTurnPhase;
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
            default: return "Unknown";
        }
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

}

enum Phase {
    SETUP,
    END_TURN,
    THROW_DICE,
    FORCE_DISCARD,
    MOVE_BANDIT,
    BUILDING
}
