import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BoardTest {
    private Board board = new Board();

    @Test
    void getDistanceTest() {
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
}
