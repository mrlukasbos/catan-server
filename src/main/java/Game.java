/*
All game mechanics
 */

import java.util.ArrayList;

class Game extends Thread {
    private boolean running = false;

    private int lastDiceThrow;
    private Board board;
    private LargestArmyAward largestArmyAward;
    private LongestRoadAward longestRoadAward;
    private ArrayList<Player> players = new ArrayList<>();
    private Player currentPlayer;
    private Interface iface;
    private int moveCount;
    private ArrayList<Event> events = new ArrayList<>();
    private Response lastResponse;

    // All gamePhases
    private DiceThrowPhase diceThrowPhase;
    private SetupPhase setupPhase;
    private InitialBuildPhase initialBuildPhase;
    private BuildPhase normalBuildPhase;
    private TradePhase tradePhase;
    private GamePhase currentPhase;
    private MoveBanditPhase moveBanditPhase;
    private ForceDiscardPhase forceDiscardPhase;

    Game(Interface iface)
    {
        this.board = new Board();
        this.iface = iface;
    }

    // start the game in a seperate thread
    synchronized void startGame() {
        init();
        print("Starting game");
        addEvent(new Event(this, EventType.GENERAL).withGeneralMessage("Starting the game"));

        if (!isAlive()) start();
    }

    void init() {
        this.running = true;
        this.board = new Board();
        this.largestArmyAward = new LargestArmyAward();
        this.longestRoadAward = new LongestRoadAward();
        lastDiceThrow = 0;
        moveCount = 0;
        events = new ArrayList<>();
        lastResponse = null;

        diceThrowPhase = new DiceThrowPhase(this, new Dice(2));
        setupPhase = new SetupPhase(this);
        initialBuildPhase = new InitialBuildPhase(this);
        normalBuildPhase = new BuildPhase(this);
        tradePhase = new TradePhase(this);
        currentPhase = new SetupPhase(this);
        moveBanditPhase = new MoveBanditPhase(this);
        forceDiscardPhase = new ForceDiscardPhase(this);
    }

    // This function gets called after start() and runs the whole game
    // Execute the current Phase and traverse to the next state
    // After every state change we signal a change, which transmits the new data to all connections
    public void run() {
        while (true) {

            // if player has disconnected
            // quit game

            if (isRunning()) {
                Phase nextPhase = currentPhase.execute();
                print("Going to phase: " + nextPhase.toString());
                currentPhase = getGamePhase(nextPhase);
            } else {
               try {
                   Thread.sleep(200);
               } catch (Exception e) {
                   e.printStackTrace();
               }
            }
        }
    }

    // Update all players with the most recent data
    // Should be called after every state, so after every dicethrow, succeeded buildcommand, etc.
    void signalGameChange() {
        System.out.println("Sending board info to players");
        getPlayers().forEach((p) -> p.send(toString()));
        iface.broadcast(toString());
    }


    // Stop the game and reset all values such that we can eventually restart it
    synchronized void quit() {
        this.board = new Board();;
        this.players = new ArrayList<>();
        this.running = false;
        print("Stopping game");
//
//        try {
//            join();
//            print("Stopped game");
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
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
            case FORCE_DISCARD:
                return forceDiscardPhase;
            case MOVE_BANDIT:
                return moveBanditPhase;
            case STEAL_CARD: //TODO: implement steal card phase after moving the bandit
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
                    "\"move_count\": " + moveCount + ", " +
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
                    "\"board\": " + getBoard().toString() + ", " +
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
        if (getWinner() == null) {
            moveCount++;
            setCurrentPlayer(getNextPlayer());
        } else {
            this.addEvent(new Event(this, EventType.GENERAL, getWinner()).withGeneralMessage(" won the game with " + getWinner().getVictoryPoints() + " points"));
            signalGameChange();
            quit();
        }
    }

    // move the currentPlayer id to the
    // +previous Player in the array.
    private Player getPreviousPlayer() {

        // we need to use floormod because % does not work for negative numbers
        return getPlayers().get(Math.floorMod(getCurrentPlayer().getId() - 1, getPlayers().size()));
    }

    void goToPreviousPlayer() {
        moveCount++;
        setCurrentPlayer(getPreviousPlayer());
    }

    Player getWinner() {
        assignLargestArmyAward();
        assignLongestRoadAward();
        for (Player player : players) {
            if (player.getVictoryPoints() >= Constants.VICTORY_POINTS_TO_WIN) {
                print("the winner is " + player.getName());
                return player;
            }
        }
        return null;
    }

    void assignLargestArmyAward() {
        for (Player player: players) {
            int amountOfKnights = player.amountOfUsedDevelopmentcard(DevelopmentCard.KNIGHT);
            if (amountOfKnights >= Constants.MINIMUM_AMOUNT_OF_KNIGHTS_FOR_AWARD && amountOfKnights > largestArmyAward.getAmountOfKnights()) {
                largestArmyAward.setPlayer(player, amountOfKnights);
            }
        }
    }

    void assignLongestRoadAward() {
        int longestRoad = 0;
        Player playerWithLongestRoad = null;
        for (Player player : players) {
            ArrayList<Edge> streets = getBoard().getStreetsFromPlayer(player);

            // iterate over every street
            int max = 0;
            for (Edge street : streets) {
                max = Math.max(findNeighbours(player, 0, getBoard().getSurroundingEdges(street), new ArrayList<Edge>()), max);
            }
            
            if (max > longestRoad) {
                longestRoad = max;
                playerWithLongestRoad = player;
            }
        }
        // TODO fix problem with equal road lengths
        longestRoadAward.setPlayer(playerWithLongestRoad);
    }

    int findNeighbours(Player player, int depth, ArrayList<Edge> stack, ArrayList<Edge> visitedEdges) {
        if (stack == null || stack.isEmpty()) return depth;
        ArrayList<Edge> neighbours = new ArrayList<>();
        for (Edge neighbour : stack) {
            if (neighbour != null && neighbour.isRoad() && (neighbour.hasPlayer() && neighbour.getPlayer() == player) && !visitedEdges.contains(neighbour)) {
                for (Edge newEdge : getBoard().getSurroundingEdges(neighbour)) {
                    neighbours.add(newEdge);
                }
                visitedEdges.add(neighbour);
            }
        }
        return findNeighbours(player,depth+1, neighbours, visitedEdges);
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

    public ArrayList<Event> getEvents() {
        return events;
    }

    public LargestArmyAward getLargestArmyAward() {
        return largestArmyAward;
    }

    public LongestRoadAward getLongestRoadAward() {
        return longestRoadAward;
    }
}

enum Phase {
    SETUP,
    INITIAL_BUILDING,
    THROW_DICE,
    FORCE_DISCARD,
    MOVE_BANDIT,
    STEAL_CARD,
    TRADING,
    BUILDING
}
