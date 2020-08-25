import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CalculateScoreTest {
    private Game game = new Game(new InterfaceServer(8888));

    @Test
    void getPlayerScore() {
        Player player = new Player(game, 1, "test");
        game.addPlayer(player);
        game.startGame();

        assertEquals(0, player.getVictoryPoints());

        Board board = game.getBoard();
        board.placeVillage(player, board.getNode("([1,2],[2,1],[2,2])"));
        board.placeCity(player, board.getNode("([3,3],[3,4],[4,3])"));
        player.addDevelopmentCard(DevelopmentCard.VICTORY_POINT);
        player.useDevelopmentCard(DevelopmentCard.VICTORY_POINT);

        assertEquals(4, player.getVictoryPoints());
    }

    @Test
    void assignLongestRoadScore() {
        Player player = new Player(game, 1, "test");
        game.addPlayer(player);
        Player player2 = new Player(game, 2, "test2");
        game.addPlayer(player2);
        game.startGame();

        // initially no one has victorypoints
        assertEquals(0, player.getVictoryPoints());
        assertEquals(0, game.getMaxRoadLength(player));
        assertEquals(0, game.getMaxRoadLength(player2));

        // short roads
        game.getBoard().placeStreet(player, game.getBoard().getEdge("([1,1],[1,2])"));
        game.getBoard().placeStreet(player, game.getBoard().getEdge("([1,2],[2,1])"));
        game.getBoard().placeStreet(player2, game.getBoard().getEdge("([3,5],[4,5])")); // start cycle
        assertEquals(2, game.getMaxRoadLength(player));
        assertEquals(1, game.getMaxRoadLength(player2));

        // no awards should be given.
        game.assignLongestRoadAward();
        assertEquals(0, player2.getVictoryPoints());
        assertEquals(0, player.getVictoryPoints());

        // player one does not have circle but has an 'easy' road
        game.getBoard().placeStreet(player, game.getBoard().getEdge("([1,2],[2,2])"));
        game.getBoard().placeStreet(player, game.getBoard().getEdge("([2,2],[2,3])"));
        game.getBoard().placeStreet(player, game.getBoard().getEdge("([2,2],[3,3])"));

        // let the road for player 2 go into a circle
        game.getBoard().placeStreet(player2, game.getBoard().getEdge("([3,4],[3,5])"));
        game.getBoard().placeStreet(player2, game.getBoard().getEdge("([2,4],[3,5])"));
        game.getBoard().placeStreet(player2, game.getBoard().getEdge("([2,5],[3,5])"));
        game.getBoard().placeStreet(player2, game.getBoard().getEdge("([2,6],[3,5])"));
        game.getBoard().placeStreet(player2, game.getBoard().getEdge("([3,5],[3,6])")); // end cycle

        assertEquals(5, game.getMaxRoadLength(player));
        assertEquals(6, game.getMaxRoadLength(player2));

        game.assignLongestRoadAward();
        assertEquals(2, player2.getVictoryPoints());
        assertEquals(0, player.getVictoryPoints());

        // player one gets an extra street, but he needs MORE to get the award
        game.getBoard().placeStreet(player, game.getBoard().getEdge("([3,2],[3,3])"));
        game.assignLongestRoadAward();
        assertEquals(2, player2.getVictoryPoints());
        assertEquals(0, player.getVictoryPoints());

        game.getBoard().placeStreet(player2, game.getBoard().getEdge("([3,6],[4,5])"));
        game.getBoard().placeStreet(player2, game.getBoard().getEdge("([2,4],[2,5])"));
        assertEquals(6, game.getMaxRoadLength(player));
        assertEquals(7, game.getMaxRoadLength(player2));

        game.assignLongestRoadAward();
        assertEquals(2, player2.getVictoryPoints());
        assertEquals(0, player.getVictoryPoints());

        game.getBoard().placeStreet(player, game.getBoard().getEdge("([3,3],[4,3])"));
        game.getBoard().placeStreet(player, game.getBoard().getEdge("([3,4],[4,3])"));
        assertEquals(8, game.getMaxRoadLength(player));
        assertEquals(7, game.getMaxRoadLength(player2));

        game.assignLongestRoadAward();
        assertEquals(0, player2.getVictoryPoints());
        assertEquals(2, player.getVictoryPoints());
    }

    @Test
    void assignLargestArmyScore() {
        Player player = new Player(game, 1, "test");
        game.addPlayer(player);
        Player player2 = new Player(game, 2, "test2");
        game.addPlayer(player2);
        game.startGame();

        assertEquals(0, player.getVictoryPoints());

        for (int i = 0; i < 2; i++) {
            player.addDevelopmentCard(DevelopmentCard.KNIGHT);
            player.useDevelopmentCard(DevelopmentCard.KNIGHT);
        }
        game.assignLargestArmyAward();

        assertEquals(0, player.getVictoryPoints());

        player.addDevelopmentCard(DevelopmentCard.KNIGHT);
        player.useDevelopmentCard(DevelopmentCard.KNIGHT);
        game.assignLargestArmyAward();

        // Only awards largest army points when a player has at least 3 armies
        assertEquals(2, player.getVictoryPoints());
        assertEquals(0, player2.getVictoryPoints());

        for (int i = 0; i < 3; i++) {
            player2.addDevelopmentCard(DevelopmentCard.KNIGHT);
            player2.useDevelopmentCard(DevelopmentCard.KNIGHT);
        }
        game.assignLargestArmyAward();

        assertEquals(2, player.getVictoryPoints());
        assertEquals(0, player2.getVictoryPoints());

        player2.addDevelopmentCard(DevelopmentCard.KNIGHT);
        player2.useDevelopmentCard(DevelopmentCard.KNIGHT);
        game.assignLargestArmyAward();

        // Only changes largest army award when player surpasses other player
        assertEquals(0, player.getVictoryPoints());
        assertEquals(2, player2.getVictoryPoints());
    }

    @Test
    void calculateVillagesScore() {
        Player player = new Player(game, 1, "test");
        game.addPlayer(player);
        game.startGame();

        assertEquals(0, player.getVictoryPoints());

        Board board = game.getBoard();
        board.placeVillage(player, board.getNode("([1,2],[2,1],[2,2])"));

        assertEquals(1, player.getVictoryPoints());
    }

    @Test
    void calculateCitiesScore() {
        Player player = new Player(game, 1, "test");
        game.addPlayer(player);
        game.startGame();

        assertEquals(0, player.getVictoryPoints());

        Board board = game.getBoard();
        board.placeCity(player, board.getNode("([3,3],[3,4],[4,3])"));

        assertEquals(2, player.getVictoryPoints());
    }

    @Test
    void calculateDevelopmentcardsScore() {
        Player player = new Player(game, 1, "test");
        game.addPlayer(player);
        game.startGame();

        assertEquals(0, player.getVictoryPoints());

        player.addDevelopmentCard(DevelopmentCard.VICTORY_POINT);
        player.useDevelopmentCard(DevelopmentCard.VICTORY_POINT);

        assertEquals(1, player.getVictoryPoints());
    }

    @Test
    void getsWinner() {
        Player player = new Player(game, 1, "test");
        game.addPlayer(player);
        game.startGame();

        assertNull(game.getWinner());

        for (int i = 0; i < 10; i++) {
            player.addDevelopmentCard(DevelopmentCard.VICTORY_POINT);
            player.useDevelopmentCard(DevelopmentCard.VICTORY_POINT);
        }

        assertEquals(player, game.getWinner());
    }
}
