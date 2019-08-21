/*
All game mechanics
 */

import java.util.ArrayList;

class Game extends Thread {
    private int lastDiceThrow = 0;
    private Board board = new Board();
    private ArrayList<Player> players = new ArrayList<Player>();
    private Player currentPlayer;
    private boolean running = false;
    private Interface iface;
    private int moveCount = 0;
    private ArrayList<Event> events = new ArrayList<>();
    private Response lastResponse = null;

    // All gamePhases
    private DiceThrowPhase diceThrowPhase = new DiceThrowPhase(this);
    private SetupPhase setupPhase = new SetupPhase(this);
    private InitialBuildPhase initialBuildPhase = new InitialBuildPhase(this);
    private BuildPhase normalBuildPhase = new BuildPhase(this);
    private TradePhase tradePhase = new TradePhase(this);
    private GamePhase currentPhase = new SetupPhase(this);

    Game(Interface iface) {
        this.iface = iface;
    }

    // start the game in a seperate thread
    synchronized void startGame() {
        init();
        print("Starting game");
        addEvent(new Event(this, EventType.GENERAL).withGeneralMessage("Starting the game"));
        start();
    }

    void init() {
        this.board = new Board();
        this.running = true;
    }

    // This function gets called after start() and runs the whole game
    // Execute the current Phase and traverse to the next state
    // After every state change we signal a change, which transmits the new data to all connections
    public void run() {
        while (true) {
            if (isRunning()) {
                Phase nextPhase = currentPhase.execute();
                print("Going to phase: " + nextPhase.toString());
                currentPhase = getGamePhase(nextPhase);
            }
        }
    }

    // Update all players with the most recent data
    // Should be called after every state, so after every dicethrow, succeeded buildcommand, etc.
    void signalGameChange() {
        getPlayers().forEach((p) -> p.send(getBoard().toString()));
        iface.broadcast(toString());
    }


    // Stop the game and reset all values such that we can eventually restart it
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

    // Convert PhaseType to GamePhase
    private GamePhase getGamePhase(Phase phase) {
        switch (phase) {
            case SETUP:
                return setupPhase;
            case INITIAL_BUILDING:
                return initialBuildPhase;
            case THROW_DICE:
                return diceThrowPhase;
            case BUILDING:
                return normalBuildPhase;
            case TRADING:
                return tradePhase;
            case MOVE_BANDIT: // for the time being
                return normalBuildPhase;
            default:
                print("We reached an unknown phase which is probably not a good thing");
                return setupPhase;
        }
    }


    @Override
    public String toString() {
        if (isRunning()) {
            return "{" +
                    "\"model\": \"game\", " +
                    "\"attributes\": {" +
                    "\"moveCount\": " + moveCount + ", " +
                    "\"players\": " + Helpers.toJSONArray(players, false) + ", " +
                    "\"status\": \"" + getGameStatus() + "\", " +
                    "\"board\": " + getBoard().toString() + ", " +
                    "\"events\": " + Helpers.toJSONArray(events, false) + ", " +
                    "\"lastDiceThrow\": " + getLastDiceThrow() + ", " +
                    "\"phase\": \"" + currentPhase.getPhaseType().toString() + "\"," +
                    "\"currentPlayer\": " + getCurrentPlayer().getId() +
                    "}" +
                    '}';
        } else {
            return "{" +
                    "\"model\": \"game\", " +
                    "\"attributes\": {" +
                    "\"players\": " + Helpers.toJSONArray(players, false) + ", " +
                    "\"status\": \"" + getGameStatus() + "\"" +
                    "}" +
                    '}';
        }
    }

    private String getGameStatus() {
        if (getPlayers().size() < Constants.MINIMUM_AMOUNT_OF_PLAYERS) {
            return "WAITING_FOR_PLAYERS";
        } else if (!isRunning()) {
            return "WAITING_FOR_TAKEOFF";
        } else {
            return "GAME_RUNNING";
        }
    }

    // Respond to user input for current player, can be an error or an acknowledgement
    void sendResponse(Response response) {
        sendResponse(getCurrentPlayer(), response);
    }

    public Response getLastResponse() {
        return lastResponse;
    }

    public void setLastResponse(Response lastResponse) {
        this.lastResponse = lastResponse;
    }

    // Respond to user input for given player, can be an error or an acknowledgement
    void sendResponse(Player player, Response response) {
        print(player.getName() + "(" + player.getId() + "): "+ response.toString());
        player.send(response.toString());
        lastResponse = response;
    }

    // move the currentPlayer id to the next Player in the array.
    private Player getNextPlayer() {
        return getPlayers().get((getCurrentPlayer().getId() + 1) % getPlayers().size());
    }

    void goToNextPlayer() {
        moveCount++;
        setCurrentPlayer(getNextPlayer());
    }

    // move the currentPlayer id to the previous Player in the array.
    private Player getPreviousPlayer() {

        // we need to use floormod because % does not work for negative numbers
        return getPlayers().get(Math.floorMod(getCurrentPlayer().getId() - 1, getPlayers().size()));
    }

    void goToPreviousPlayer() {
        moveCount++;
        setCurrentPlayer(getPreviousPlayer());
    }

    Player getWinner() {
        for (Player player : players) {
            if (player.getAllVictoryPoints() >= Constants.VICTORY_POINTS_TO_WIN) {
                print("the winner is " + player.getName());
                return player;
            }
        }
        return null;
    }


    // Getters and Setters

    void addEvent(Event event) { events.add(event); }

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

    int getMoveCount() {
        return moveCount;
    }

    int getLastDiceThrow() { return lastDiceThrow; }

    void setLastDiceThrow(int diceThrow) { lastDiceThrow = diceThrow; }




}

enum Phase {
    SETUP,
    INITIAL_BUILDING,
    THROW_DICE,
    FORCE_DISCARD,
    MOVE_BANDIT,
    TRADING,
    BUILDING
}
