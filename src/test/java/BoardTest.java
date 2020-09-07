import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest {
    private WebSocketConnectionServer iface = new WebSocketConnectionServer(10007);
    private Game game = new Game(iface);
    private BuildPhase buildPhase = new BuildPhase(game);
    private  Player player = new Player(game,0, "tester");
    private  Player player2 = new Player(game,1, "tester1");

    @Test
    void getDistanceTest() {
        Board board = game.getBoard();

        Node a = board.getNode("([2,2],[3,1],[3,2])");
        Node b = board.getNode("([2,1],[2,2],[3,1])");
        assertEquals(board.getDistance(a, b), 1);

        a = board.getNode("([1,0],[1,1],[2,1])");
        b = board.getNode("([3,6],[4,5],[4,6])");
        assertEquals(board.getDistance(a, b), 10);

        a = board.getNode("([0,4],[1,4],[1,5])");
        b = board.getNode("([3,6],[4,5],[4,6])");
        assertEquals(board.getDistance(a, b), 7);
    }

    @Test
    void harbourTest(){
        Board board = game.getBoard();
        assertFalse(board.playerHasHarbour(player));
        assertFalse(board.playerHasHarbour(player, HarbourType.HARBOUR_ALL));

        // this edge should be a harbour
        Edge harbourEdge = board.getEdge("([1,0],[2,1])");
        assertTrue(harbourEdge.isHarbour());

        // get the nodes next to this edge
        Node nodeToPlaceVillage = board.getSurroundingNodes(harbourEdge).get(0);
        assertTrue(board.nodeIsHarbour(nodeToPlaceVillage));

        board.placeVillage(player, nodeToPlaceVillage);
        assertTrue(board.playerHasHarbour(player));
        assertTrue(board.playerHasHarbour(player, harbourEdge.getHarbourType()));
    }
}
