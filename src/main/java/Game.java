/*
All game mechanics
 */

import java.util.ArrayList;

class Game {
    private int dice = 0;
    private Board board;
    private ArrayList<Player> players;
    private Player currentPlayer;
    private Phase phase;
    private boolean running;

    Game() {  }

    void start(ArrayList<Player> players) {
        this.phase = Phase.SETUP;
        this.board = new Board(players);
        this.players = new ArrayList<Player>(players); // this copies the arraylist
        running = true;
        print("Starting game");
    }

    void stop() {
        this.board = null;
        this.players = null;
        running = false;
        print("Stopping game");
    }

    void run() {
        switch (phase) {

            /*
            Determine the player that can start
             */
            case SETUP: {
                currentPlayer = determineFirstPlayer();
                goToPhase(Phase.THROW_DICE);
                break;
            }

            /*
            Throw a dice. If it is 7 then move the bandit
            Otherwise give the players their resources.
             */
            case THROW_DICE: {
                int diceThrow = currentPlayer.throwDice();
                print("Dice thrown: " + diceThrow);
                if (diceThrow == 7) {
                    goToPhase(Phase.FORCE_DISCARD);
                } else {
                    distributeResourcesForDice(diceThrow);
                    goToPhase(Phase.BUILDING);
                }
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
                // example keys
                // ([3,0],[4,0],[4,1])
                // ([3,2],[4,1],[4,2])

                currentPlayer.send("Please build if you like.");
                String nodeKey = currentPlayer.listen();
                print("Received message from player " + currentPlayer.getName()+ ": " + nodeKey);

                if (board.getNode(nodeKey) != null) {
                    board.placeVillage(currentPlayer, board.getNode(nodeKey));
                } else {
                    print("Received invalid key!");
                }
                break;
            }
        }

        Player currentPlayer = progressToNextPlayer();
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
        this.phase = phase;
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
        currentPlayer = firstPlayer;
        return firstPlayer;
    }

    // move the currentPlayer id to the next Player in the array.
    Player progressToNextPlayer() {
        Player nextPlayer = players.get((currentPlayer.getId() + 1) % players.size());
        currentPlayer = nextPlayer;
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

    public boolean isRunning() {
        return running;
    }

    private void print(String msg) {
        System.out.println("[Game] \t \t" + msg);
    }
}

enum Phase {
    SETUP,
    THROW_DICE,
    FORCE_DISCARD,
    MOVE_BANDIT,
    GATHER_RESOURCES,
    BUILDING,
    PLAYING_CARD
}