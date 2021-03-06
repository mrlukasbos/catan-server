package game;

import board.*;
import communication.WebSocketConnectionServer;
import game.phases.*;
import utils.Constants;
import utils.Helpers;

import java.util.ArrayList;

public class Game extends Thread {
    private boolean running = false;

    private int lastDiceThrow;
    private Board board;
    private LargestArmyAward largestArmyAward;
    private LongestRoadAward longestRoadAward;
    private ArrayList<Player> players = new ArrayList<>();
    private Player currentPlayer;
    private WebSocketConnectionServer iface;
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

    public Game(WebSocketConnectionServer iface) {
        this.board = new Board();
        this.iface = iface;
    }

    // start the game in a seperate thread
    public synchronized void startGame() {
        init();
        print("Starting game");
        addEvent(new Event(this, EventType.GENERAL).withGeneralMessage("Starting the game"));
        signalGameChange();
        if (!isAlive()) start();
    }

    public void init() {
        this.running = true;
        this.board = new Board();
        this.largestArmyAward = new LargestArmyAward();
        this.longestRoadAward = new LongestRoadAward();
        lastDiceThrow = 0;
        moveCount = 0;
        events = new ArrayList<>();
        lastResponse = null;

        diceThrowPhase = new DiceThrowPhase(this, new Dice(2));
        setupPhase = new SetupPhase(this, new Dice(2));
        initialBuildPhase = new InitialBuildPhase(this);
        normalBuildPhase = new BuildPhase(this);
        tradePhase = new TradePhase(this);
        currentPhase = setupPhase;
        moveBanditPhase = new MoveBanditPhase(this);
        forceDiscardPhase = new ForceDiscardPhase(this);
    }

    // This function gets called after start() and runs the whole game
    // Execute the current game.Phase and traverse to the next state
    // After every state change we signal a change, which transmits the new data to all connections
    public void run() {

        try {
            while (isAlive() && isRunning() && !getPlayers().isEmpty()) {
                Phase nextPhase = currentPhase.execute();
                print("Going to phase: " + nextPhase.toString());
                currentPhase = getGamePhase(nextPhase);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    // Update all players with the most recent data
    // Should be called after every state, so after every dicethrow, succeeded buildcommand, etc.
    public void signalGameChange() {
        print("Sending board info to players");
        getPlayers().forEach((p) -> p.send(toString()));
        if (iface != null) iface.broadcast(toString());
    }


    // Stop the game and reset all values such that we can eventually restart it
    void quit() {
        print("closing sockets");
        for(Player player : players) {
            player.stop();
        }

        print("Stop running");
        this.running = false;

        print("Joining thread");
        try {
            join();
        } catch (Exception e) {
            e.printStackTrace();
        }

        print("Stopped game");
    }

    // Convert PhaseType to game.GamePhase
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
                    "\"currentPlayer\": " + (getCurrentPlayer() != null ? getCurrentPlayer().getId() : "-1") +
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
    public void sendResponse(Response response) {
        print("sending response to player " + getCurrentPlayer().getName() + ": " + response.getTitle());
        sendResponse(getCurrentPlayer(), response);
    }

    public Response getLastResponse() {
        return lastResponse;
    }

    public void setLastResponse(Response lastResponse) {
        this.lastResponse = lastResponse;
    }

    // Respond to user input for given player, can be an error or an acknowledgement
    public void sendResponse(Player player, Response response) {
        print(player.getName() + "(" + player.getId() + "): "+ response.toString());
        player.send(response.toString());
        lastResponse = response;
    }

    // move the currentPlayer id to the next game.Player in the array.
    private Player getNextPlayer() {
        return getPlayers().get((getCurrentPlayer().getId() + 1) % getPlayers().size());
    }

    public void goToNextPlayer() {
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
    // +previous game.Player in the array.
    private Player getPreviousPlayer() {

        // we need to use floormod because % does not work for negative numbers
        return getPlayers().get(Math.floorMod(getCurrentPlayer().getId() - 1, getPlayers().size()));
    }

    public void goToPreviousPlayer() {
        moveCount++;
        setCurrentPlayer(getPreviousPlayer());
    }

    public Player getWinner() {
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

    public void assignLargestArmyAward() {
        for (Player player: players) {
            int amountOfKnights = player.amountOfUsedDevelopmentcard(DevelopmentCard.KNIGHT);
            if (amountOfKnights >= Constants.MINIMUM_AMOUNT_OF_KNIGHTS_FOR_AWARD && amountOfKnights > largestArmyAward.getAmountOfKnights()) {
                largestArmyAward.setPlayer(player, amountOfKnights);
            }
        }
    }

    public void assignLongestRoadAward() {
        int longestRoad = 0;
        Player playerWithLongestRoad = null;
        for (Player player : players) {
            int roadLength = getMaxRoadLength(player);
            if (roadLength > longestRoad) {
                longestRoad = roadLength;
                playerWithLongestRoad = player;
            }
        }

        if (longestRoad >= Constants.MINIMUM_AMOUNT_OF_ROADS_FOR_AWARD && longestRoad > longestRoadAward.getAmountOfRoads()) {
            longestRoadAward.setPlayer(playerWithLongestRoad);
            longestRoadAward.setAmountOfRoads(longestRoad);
        }
    }

    public int getMaxRoadLength(Player player) {
        ArrayList<Edge> streets = getBoard().getStreetsFromPlayer(player);

        // iterate over every street
        int max = 0;
        for (Edge street : streets) {
            ArrayList<EdgeTrace> edgeTraces = new ArrayList<>();
            edgeTraces.add(new EdgeTrace(street, null, new ArrayList<>()));
            max = Math.max(findNeighbours(player, 0, edgeTraces), max);
        }
        return max;
    }

    // get all nodes connected to player streets
    // for all nodes find neighbour nodes and store visited nodes
    int findNeighbours(Player player, int depth, ArrayList<EdgeTrace> stack) {
        if (stack == null || stack.isEmpty()) return depth - 1;

        ArrayList<EdgeTrace> neighbours = new ArrayList<>();
        for (EdgeTrace neighbourEg : stack) {
            Edge neighbour = neighbourEg.edge;
            Edge prev = neighbourEg.prev;
            ArrayList<Edge> trace = neighbourEg.trace;


            if (neighbour != null && neighbour.isRoad() && (neighbour.hasPlayer() && neighbour.getPlayer() == player) && !trace.contains(neighbour)) {
                for (Edge newEdge : getBoard().getSurroundingEdges(neighbour)) {

                    // the surrounding edge must not be a surrounding edge of the previous edge
                    if (prev == null || !getBoard().getSurroundingEdges(prev).contains(newEdge)) {
                        ArrayList<Edge> t = new ArrayList<>(trace);
                        t.add(neighbour);
                        EdgeTrace newEdgeTrace = new EdgeTrace (newEdge, neighbour, t);
                        neighbours.add(newEdgeTrace);
                    }
                }
            }
        }
        return findNeighbours(player,depth+1, neighbours);
    }

    public int getRequiredAmountOfCardsToTrade(Player player, Resource resourceFrom) {
        int requiredResourcesForBankTrade =  Constants.MINIMUM_CARDS_FOR_TRADE; // default (harbours change this)
        if (getBoard().playerHasHarbour(player, HarbourType.HARBOUR_ALL)) {
            requiredResourcesForBankTrade = 3;
        } else if (getBoard().playerHasHarbour(player)) {
            if (getBoard().playerHasHarbour(player, Constants.RESOURCES_HARBOURS.get(resourceFrom))) {
                requiredResourcesForBankTrade = 2;
            }
        }
        return requiredResourcesForBankTrade;
    }

    // Getters and Setters

    public void addEvent(Event event) { events.add(event); }

    public void addPlayer(Player p) {
        players.add(p);
        signalGameChange();
    }

    public ArrayList<Player> getPlayers() { return players; }

    public Board getBoard() { return board; }

    public boolean isRunning() { return running; }

    public void print(String msg) {
        System.out.println("[game.Game] \t \t" + msg);
    }

    public void setCurrentPlayer(Player player) { currentPlayer = player; }

    public Player getCurrentPlayer() { return currentPlayer; }

    int getMoveCount() {
        return moveCount;
    }

    int getLastDiceThrow() { return lastDiceThrow; }

    public void setLastDiceThrow(int diceThrow) { lastDiceThrow = diceThrow; }

    public ArrayList<Event> getEvents() {
        return events;
    }

    public LargestArmyAward getLargestArmyAward() {
        return largestArmyAward;
    }

    public LongestRoadAward getLongestRoadAward() {
        return longestRoadAward;
    }

    public void removePlayer(Player p) {
        this.players.remove(p);
    }
}

class EdgeTrace {
    Edge edge;
    Edge prev;
    ArrayList<Edge> trace;
    EdgeTrace(Edge edge, Edge prev, ArrayList<Edge> trace){
        this.edge = edge;
        this.trace = trace;
        this.prev = prev;
    }
}

