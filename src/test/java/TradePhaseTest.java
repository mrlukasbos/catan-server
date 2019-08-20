import com.google.gson.JsonArray;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TradePhaseTest {
    Interface iface = new Interface(10007);
    Game game = new Game(iface);
    TradePhase tradePhase = new TradePhase(game);
    Player player = new Player(game,0, "tester");

    @Test
    public void testTradingNothing() {
        String message = "[]";
        JsonArray jsonArray = new jsonValidator().getJsonIfValid(player, message);
        assertTrue(tradePhase.tradeIsValid(player, jsonArray));
    }

    @Test
    public void testTradingWithSufficientResources() {
        game.setCurrentPlayer(player);
        player.addResources(Resource.ORE, 4);

        String message = "[{ \"from\": \"ore\", \"to\": \"wood\" }]";
        JsonArray jsonArray = new jsonValidator().getJsonIfValid(player, message);
        assertTrue(tradePhase.tradeIsValid(player, jsonArray));
    }

    /*
     * The following test tests the edge-case that when you do two trades,
     * some of them can still be incomplete, but can be completed later because of other trades you also do.
     *
     * This means: if you trade the 4 grain for an ore, you are then allowed to trade the ore for whatever.
     */
    @Test
    public void testTradingWithSufficientResourcesForTwoCommandsWitOneFirstIncomplete() {
        game.setCurrentPlayer(player);
        player.addResources(Resource.GRAIN, 4);
        player.addResources(Resource.ORE, 3);

        String message = "[{ \"from\": \"ore\", \"to\": \"stone\" }, { \"from\": \"grain\", \"to\": \"ore\" }]";
        JsonArray jsonArray = new jsonValidator().getJsonIfValid(player, message);
        assertTrue(tradePhase.tradeIsValid(player, jsonArray));
    }

    @Test
    public void testTradingWithSufficientResourcesForTwoCommandsWithBothIncomplete() {
        game.setCurrentPlayer(player);
        player.addResources(Resource.GRAIN, 3);
        player.addResources(Resource.ORE, 3);

        String message = "[{ \"from\": \"ore\", \"to\": \"stone\" }, { \"from\": \"grain\", \"to\": \"ore\" }]";
        JsonArray jsonArray = new jsonValidator().getJsonIfValid(player, message);
        assertFalse(tradePhase.tradeIsValid(player, jsonArray));
        assertEquals(game.getLastResponse().getCode(), Constants.NOTENOUGHRESOURCESERROR.getCode());
    }

    @Test
    public void testTradingWithInSufficientResources() {
        game.setCurrentPlayer(player);
        player.addResources(Resource.ORE, 3);

        String message = "[{ \"from\": \"ore\", \"to\": \"wood\" }]";
        JsonArray jsonArray = new jsonValidator().getJsonIfValid(player, message);
        assertFalse(tradePhase.tradeIsValid(player, jsonArray));
        assertEquals(game.getLastResponse().getCode(), Constants.NOTENOUGHRESOURCESERROR.getCode());
    }

    @Test
    public void testTradingWithInSufficientResourcesForTwoCommands() {
        game.setCurrentPlayer(player);
        player.addResources(Resource.GRAIN, 7);

        String message = "[{ \"from\": \"grain\", \"to\": \"wood\" }, { \"from\": \"grain\", \"to\": \"ore\" }]";
        JsonArray jsonArray = new jsonValidator().getJsonIfValid(player, message);
        assertFalse(tradePhase.tradeIsValid(player, jsonArray));
        assertEquals(game.getLastResponse().getCode(), Constants.NOTENOUGHRESOURCESERROR.getCode());
    }

    @Test
    public void testWithInvalidResource() {
        game.setCurrentPlayer(player);
        player.addResources(Resource.ORE, 4);

        String message = "[{ \"from\": \"lore\", \"to\": \"wood\" }]";
        JsonArray jsonArray = new jsonValidator().getJsonIfValid(player, message);
        assertFalse(tradePhase.tradeIsValid(player, jsonArray));
        assertEquals(game.getLastResponse().getCode(), Constants.INVALID_TRADE_ERROR.getCode());
    }

    @Test
    public void testWithInvalidJsonKey() {
        game.setCurrentPlayer(player);
        player.addResources(Resource.ORE, 4);

        String message = "[{ \"form\": \"lore\", \"to\": \"wood\" }]";
        JsonArray jsonArray = new jsonValidator().getJsonIfValid(player, message);
        assertFalse(tradePhase.tradeIsValid(player, jsonArray));
        assertEquals(game.getLastResponse().getCode(), Constants.INVALID_TRADE_ERROR.getCode());
    }

}
