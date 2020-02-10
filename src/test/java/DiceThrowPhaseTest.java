import com.google.gson.JsonArray;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DiceThrowPhaseTest {
    private Interface iface = new Interface(10007);
    private Game game = new Game(iface);
    private DiceStub diceStub = new DiceStub();
    private DiceThrowPhase diceThrowPhase = new DiceThrowPhase(game, diceStub);
    private  Player player = new Player(game,0, "tester");
    private  Player player2 = new Player(game,1, "tester1");

    @BeforeEach
    void beforeTest() {
        game.addPlayer(player);
        game.addPlayer(player2);
        game.setCurrentPlayer(player);
        game.init();
    }

    @Test
    void phaseNameShouldBeCorrectTest() {
        assertEquals(diceThrowPhase.getPhaseType(), Phase.THROW_DICE);
    }

    @Test
    void nextPhaseIsForceDiscardTest() {
        diceStub.shouldThrow(7);
        Phase nextPhase = diceThrowPhase.execute();
        assertEquals(Phase.FORCE_DISCARD, nextPhase);
    }

    @Test
    void nextPhaseIsTradingTest() {
        diceStub.shouldThrow(8);
        Phase nextPhase = diceThrowPhase.execute();
        assertEquals(Phase.TRADING, nextPhase);
    }

    @Test
    void shouldDistributeResourcesToVillagesForDiceTest() {
        Tile tile = game.getBoard().getTilesForDiceNumber(8).get(0);
        game.getBoard().placeVillage(player, tile.getNodes().get(0));

        assertEquals(0, player.countResources(tile.produces()));
        diceStub.shouldThrow(8);
        diceThrowPhase.execute();
        assertEquals(1, player.countResources(tile.produces()));
    }

    @Test
    void shouldDistributeResourcesToMultipleVillagesForDiceTest() {
        Tile tile = game.getBoard().getTilesForDiceNumber(8).get(0);
        game.getBoard().placeVillage(player, tile.getNodes().get(0));
        game.getBoard().placeVillage(player, tile.getNodes().get(2));

        assertEquals(0, player.countResources(tile.produces()));
        diceStub.shouldThrow(8);
        diceThrowPhase.execute();
        assertEquals(2, player.countResources(tile.produces()));
    }

    @Test
    void shouldDistributeResourcesToMultiplePlayersForDiceTest() {
        Tile tile = game.getBoard().getTilesForDiceNumber(8).get(0);
        game.getBoard().placeVillage(player, tile.getNodes().get(0));
        game.getBoard().placeVillage(player2, tile.getNodes().get(2));

        assertEquals(0, player.countResources(tile.produces()));
        assertEquals(0, player2.countResources(tile.produces()));
        diceStub.shouldThrow(8);
        diceThrowPhase.execute();
        assertEquals(1, player.countResources(tile.produces()));
        assertEquals(1, player2.countResources(tile.produces()));
    }

    @Test
    void shouldDistributeResourcesToCitiesForDiceTest() {
        Tile tile = game.getBoard().getTilesForDiceNumber(8).get(0);
        game.getBoard().placeCity(player, tile.getNodes().get(0));

        assertEquals(0, player.countResources(tile.produces()));
        diceStub.shouldThrow(8);
        diceThrowPhase.execute();
        assertEquals(2, player.countResources(tile.produces()));
    }

    @Test
    void shouldNotDistributeResourcesForBanditPosition() {
        Tile tile = game.getBoard().getTilesForDiceNumber(8).get(0);
        game.getBoard().placeCity(player, tile.getNodes().get(0));
        game.getBoard().getBandit().setTile(tile);

        assertEquals(0, player.countResources(tile.produces()));
        diceStub.shouldThrow(8);
        diceThrowPhase.execute();
        assertEquals(0, player.countResources(tile.produces()));
    }

    @Test
    void shouldAddEventsForDiceThrowTest() {
        Tile tile = game.getBoard().getTilesForDiceNumber(8).get(0);
        game.getBoard().placeVillage(player2, tile.getNodes().get(0));

        diceStub.shouldThrow(8);
        diceThrowPhase.execute();
        ArrayList<Event> events = game.getEvents();
        assert(events.get(events.size()-1).toString().contains("\"resources\": [{\"type\":\"" + tile.produces().toString() + "\", \"value\":1}]"));
    }
}


