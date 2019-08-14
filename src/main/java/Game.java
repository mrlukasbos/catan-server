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

    public void run() {
        while (true) {
            if (isRunning()) {
                    switch (phase) {

                    /*
                    Determine the player that can start
                     */
                    case SETUP: {
                        currentPlayer = determineFirstPlayer();
                        signalGameChange();
                        goToPhase(Phase.THROW_DICE);
                        break;
                    }

                    /*
                    Throw a dice. If it is 7 then move the bandit
                    Otherwise give the players their resources.
                     */
                    case THROW_DICE: {
                        Dice d = new Dice(2);
                        dice = d.throwDice();
                        print("Dice thrown: " + dice);
                        if (dice == 7) {
                            goToPhase(Phase.FORCE_DISCARD);
                        } else {
                            distributeResourcesForDice(dice);
                            goToPhase(Phase.BUILDING);
                        }
                        signalGameChange();
                        break;
                    }

                    case FORCE_DISCARD: {
                        for (Player p : getPlayers()) {
                            if (p.countResources() > 7) {

                            }
                        }
                        goToPhase(Phase.MOVE_BANDIT);
                        break;
                    }

                    case MOVE_BANDIT: {
                        goToPhase(Phase.BUILDING);
                        break;
                    }

                    case BUILDING: {
                        build();
                        goToPhase(Phase.END_TURN);
                        break;
                    }

                    case END_TURN: {
                        currentPlayer = getNextPlayer();
                        print("next player: " + currentPlayer.getName());
                        signalGameChange();
                        goToPhase(Phase.THROW_DICE);

                    }
                }
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


    // Example inputs
    // [{ "structure": "village", "location": "([1,2],[2,1],[2,2])" }]
    // [{ "structure": "city", "location": "([1,2],[2,1],[2,2])" }]

    // [{ "structure": "village", "location": "([2,2],[3,1],[3,2])" }]
    // [{ "structure": "city", "location": "([2,2],[3,1],[3,2])" }]

    // [{ "structure": "street", "location": "([2,2],[3,1])" }]
    // [{ "structure": "street", "location": "([3,1],[3,2])" }, { "structure": "street", "location": "([2,2],[3,2])" }]
    private void build() {
        currentPlayer.send("Please build if you like.");

        String message = currentPlayer.listen();
        if (message != null) { // the message is ready
            print("Received message from player " + currentPlayer.getName() + ": " + message);

            JsonArray jsonArray = new JsonParser().parse(message).getAsJsonArray();

            for (JsonElement element : jsonArray) {
                JsonObject object = element.getAsJsonObject();

                String structureString = object.get("structure").getAsString();
                Structure structure = stringToStructure(structureString);
                String key = object.get("location").getAsString();

                if (!isLegal(key)) {
                    print("Received message with illegal location (key): " + key);
                    break;
                }

                if (!isLegal(structure)) {
                    print("Received message with illegal structure: " + structureString);
                    break;
                }

                board.placeStructure(currentPlayer, structure, key);
            }
        }
    }

    private boolean isLegal(String key) {
        return board.hasEdge(key) || board.hasNode(key);
    }

    private boolean isLegal(Structure struct) {
        return struct != Structure.NONE;
    }

    private Structure stringToStructure(String str) {
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

    private void distributeResourcesForDice(int diceThrow) {
        for (Node node : getBoard().getNodes()) {
            if (node.hasPlayer() && node.hasStructure()) {
                for (Tile tile : node.getTiles()) {
                    if (tile.isTerrain() && tile.getNumber() == diceThrow) {
                        int amount = node.getStructure() == Structure.CITY ? 2 : 1;
                        node.getPlayer().addResources(tile.typeToResource(tile.getType()), amount);
                    }
                }
            }
        }
    }

    void goToPhase(Phase phase) {
        print ("going to phase: " + phaseToString(phase));
        this.phase = phase;
    }



    // throw the dice, the highest throw can start
    private Player determineFirstPlayer() {
        Dice dice = new Dice(1);
        int highestDiceThrow = 0;
        Player firstPlayer = players.get(0);
        for (Player player : players) {
            int newDiceThrow = dice.throwDice();
            if (newDiceThrow > highestDiceThrow) {
                firstPlayer = player;
                highestDiceThrow = newDiceThrow;
            }
        }
        currentPlayer = firstPlayer;
        return firstPlayer;
    }

    // move the currentPlayer id to the next Player in the array.
    private Player getNextPlayer() {
        Player nextPlayer = players.get((currentPlayer.getId() + 1) % players.size());
        return nextPlayer;
    }

    ArrayList<Player> getPlayers() {
        return players;
    }

    void addPlayer(Player p) {
        players.add(p);
    }

    Board getBoard() {
        return board;
    }

    boolean isRunning() {
        return running;
    }

    private void print(String msg) {
        System.out.println("[Game] \t \t" + msg);
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

    int getLastDiceThrow() {
        return dice;
    }
}

enum Phase {
    SETUP,
    END_TURN,
    THROW_DICE,
    FORCE_DISCARD,
    MOVE_BANDIT,
    GATHER_RESOURCES,
    BUILDING,
    PLAYING_CARD,

}