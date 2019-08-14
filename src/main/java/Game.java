/*
All game mechanics
 */

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;

class Game extends Thread {
    private int dice = 0;
    private Board board;
    private ArrayList<Player> players;
    private Player currentPlayer;
    private Phase phase;
    private boolean running = false;
    private Interface iface;

    // all gamePhases
    DiceThrowPhase diceThrowPhase = new DiceThrowPhase(this);
    SetupPhase setupPhase = new SetupPhase(this);
    BuildPhase buildPhase = new BuildPhase(this);

    Game(Interface iface) {
        this.iface = iface;
    }

    synchronized void start(ArrayList<Player> players) {
        this.phase = Phase.SETUP;
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

                currentPhase = currentPhase.execute();
                signalGameChange();

//                    case THROW_DICE: {
//                        Phase nextPhase = diceThrowPhase.execute();
//                        signalGameChange();
//                        goToPhase(nextPhase);
//                        break;
//                    }
//
//                    case FORCE_DISCARD: {
//                        for (Player p : getPlayers()) {
//                            if (p.countResources() > 7) {
//
//                            }
//                        }
//                        goToPhase(Phase.MOVE_BANDIT);
//                        break;
//                    }
//
//                    case MOVE_BANDIT: {
//                        goToPhase(Phase.BUILDING);
//                        break;
//                    }
//
//                    case BUILDING: {
//                        build();
//                        goToPhase(Phase.END_TURN);
//                        break;
//                    }
//
//                    case END_TURN: {
//                        currentPlayer = getNextPlayer();
//                        print("next player: " + currentPlayer.getName());
//                        signalGameChange();
//                        goToPhase(Phase.THROW_DICE);
//
//                    }
                //               }
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

    private void goToPhase(Phase phase) {
        print("going to phase: " + phaseToString(phase));
        this.phase = phase;
    }


    // move the currentPlayer id to the next Player in the array.
    private Player getNextPlayer() {
        return players.get((currentPlayer.getId() + 1) % players.size());
    }

    ArrayList<Player> getPlayers() {
        return players;
    }

    Board getBoard() {
        return board;
    }

    boolean isRunning() {
        return running;
    }

    void print(String msg) {
        System.out.println("[Game] \t \t" + msg);
    }


    void setCurrentPlayer(Player player) {
        currentPlayer = player;
    }

    Player getCurrentPlayer() {
        return currentPlayer;
    }

    int getLastDiceThrow() {
        return dice;
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


    GamePhase getGamePhase(Phase phase) {
        switch (phase) {
            case SETUP:
                return setupPhase;
            case THROW_DICE:
                return diceThrowPhase;
            case BUILDING:
                return buildPhase;
            default:
                return setupPhase;
        }
    }


    String phaseToString(Phase phase) {
        switch (phase) {
            case SETUP: return "SETUP";
            case THROW_DICE: return "THROW_DICE";
            case FORCE_DISCARD: return "FORCE_DISCARD";
            case MOVE_BANDIT: return "MOVE_BANDIT";
            case BUILDING: return "BUILDING";
            default: return "Unknown";
        }
    }
}

enum Phase {
    SETUP,
    END_TURN,
    THROW_DICE,
    FORCE_DISCARD,
    MOVE_BANDIT,
    BUILDING,
    PLAYING_CARD,
}
