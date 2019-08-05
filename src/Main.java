import java.io.IOException;

public class Main {

    public static void main(String[] args) {

        int amountOfPlayers = 2;

        // Boot the server
        Server server;
        try {
            server = new Server(10006, amountOfPlayers);
            server.start();


        // start the game
        Game game = new Game(amountOfPlayers);
        System.out.println("New game created.");

        /*
         * Start of the game with the player that throws highest with the dice.
         * We can decide that here already.
         */
        Player currentPlayer = game.determineFirstPlayer();
        System.out.println("The first player is: " + currentPlayer.getName());

        /*
         * Now we can start the game. The first round everyone can create two villages and two streets.
         * The first player can choose one village and one street first, then the second player, third, etc...
         * and then the second sets of villages and streets are chosen in reversed order.
         *
         * The resources belonging to the second villages can be received immediately in the first round.
         */


        /*
         * A new player can start doing things. the first thing to do is a Dice throw.
         * If it is a 7, then we need to go to barbarian phase, otherwise collect resources
         */
        int dice = currentPlayer.throwDice();

        if (dice == 7) {
            // players should maybe give away resources
            // game.barbarian
        } else {

        }

        currentPlayer.addResources(Resources.GRAIN, 4);
        currentPlayer.removeResources(Resources.GRAIN, 3);


        /*
         * Progress to to next player to do his move.
         */
        currentPlayer = game.progressToNextPlayer();

        server.shutDown();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
