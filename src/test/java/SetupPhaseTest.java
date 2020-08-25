import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;

public class SetupPhaseTest {
    private InterfaceServer iface = new InterfaceServer(10007);
    private Game game = new Game(iface);
    private DiceStub diceStub = new DiceStub();
    private SetupPhase setupPhase = new SetupPhase(game, diceStub);
    private  Player player = new Player(game,0, "tester");
    private  Player player2 = new Player(game,1, "tester1");
    private  Player player3 = new Player(game,2, "tester2");
    private  Player player4 = new Player(game,3, "tester3");

    @BeforeEach
    void beforeTest() {
        game.addPlayer(player);
        game.addPlayer(player2);
        game.addPlayer(player3);
        game.addPlayer(player4);
        game.setCurrentPlayer(player);
        game.init();
    }

    @Test
    void thePlayerWithTheHighestDiceThrowWins() {
        ArrayList<Integer> order = new ArrayList<>() {{
                add(10);
                add(9);
                add(6);
                add(7);
            }};
        diceStub.shouldThrowInOrder(order);
        Player firstplayer = setupPhase.determineFirstPlayer();
        assertEquals(player, firstplayer);


    }

    @Test
    void itExecutes() {
        ArrayList<Integer> order = new ArrayList<>() {{
            add(10);
            add(9);
            add(12);
            add(7);
        }};
        diceStub.shouldThrowInOrder(order);
        Phase phase = setupPhase.execute();
        assertEquals(player3, game.getCurrentPlayer());
        assertEquals(Phase.INITIAL_BUILDING, phase);
    }


}

