import com.google.gson.JsonArray;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TradePhaseTest {
    InterfaceServer iface = new InterfaceServer(10007);
    Game game = new Game(iface);
    TradePhase tradePhase = new TradePhase(game);
    PlayerStub player = new PlayerStub(game,0, "tester");

    @Test
    public void testTradingNothing() {
        String message = "[]";
        JsonArray jsonArray = jsonValidator.getAsJsonObject(message);
        assertTrue(tradePhase.tradeIsValid(player, jsonArray));
    }

    @Test
    public void testTradingWithSufficientResources() {
        game.setCurrentPlayer(player);
        player.addResources(Resource.ORE, 4);

        String message = "[{ \"from\": \"ore\", \"to\": \"wood\" }]";
        JsonArray jsonArray = jsonValidator.getAsJsonObject(message);
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
        JsonArray jsonArray = jsonValidator.getAsJsonObject(message);
        assertTrue(tradePhase.tradeIsValid(player, jsonArray));
    }

    @Test
    public void testTradingWithSufficientResourcesForTwoCommandsWithBothIncomplete() {
        game.setCurrentPlayer(player);
        player.addResources(Resource.GRAIN, 3);
        player.addResources(Resource.ORE, 3);

        String message = "[{ \"from\": \"ore\", \"to\": \"stone\" }, { \"from\": \"grain\", \"to\": \"ore\" }]";
        JsonArray jsonArray = jsonValidator.getAsJsonObject(message);
        assertFalse(tradePhase.tradeIsValid(player, jsonArray));
        assertEquals(game.getLastResponse().getCode(), Constants.INSUFFICIENT_RESOURCES_ERROR.getCode());
    }

    @Test
    public void testTradingWithInSufficientResources() {
        game.setCurrentPlayer(player);
        player.addResources(Resource.ORE, 3);

        String message = "[{ \"from\": \"ore\", \"to\": \"wood\" }]";
        JsonArray jsonArray = jsonValidator.getAsJsonObject(message);
        assertFalse(tradePhase.tradeIsValid(player, jsonArray));
        assertEquals(game.getLastResponse().getCode(), Constants.INSUFFICIENT_RESOURCES_ERROR.getCode());
    }

    @Test
    public void testTradingWithInSufficientResourcesForTwoCommands() {
        game.setCurrentPlayer(player);
        player.addResources(Resource.GRAIN, 7);

        String message = "[{ \"from\": \"grain\", \"to\": \"wood\" }, { \"from\": \"grain\", \"to\": \"ore\" }]";
        JsonArray jsonArray = jsonValidator.getAsJsonObject(message);
        assertFalse(tradePhase.tradeIsValid(player, jsonArray));
        assertEquals(game.getLastResponse().getCode(), Constants.INSUFFICIENT_RESOURCES_ERROR.getCode());
    }

    @Test
    public void testWithInvalidResource() {
        game.setCurrentPlayer(player);
        player.addResources(Resource.ORE, 4);

        String message = "[{ \"from\": \"lore\", \"to\": \"wood\" }]";
        JsonArray jsonArray = jsonValidator.getAsJsonObject(message);
        assertFalse(tradePhase.tradeIsValid(player, jsonArray));
        assertEquals(game.getLastResponse().getCode(), Constants.INVALID_TRADE_ERROR.getCode());
    }

//    @Test
//    public void testWithInvalidJsonKey() {
//        game.setCurrentPlayer(player);
//        player.addResources(Resource.ORE, 4);
//
//        String message = "[{ \"form\": \"lore\", \"to\": \"wood\" }]";
//        JsonArray jsonArray = jsonValidator.getAsJsonObject(message);
//        assertFalse(tradePhase.tradeIsValid(player, jsonArray));
//        assertEquals(game.getLastResponse().getCode(), Constants.MALFORMED_JSON_ERROR.getCode());
//    }

    @Test
    public void testTradingOnBoard() {
        game.setCurrentPlayer(player);
        player.addResources(Resource.ORE, 4);

        String message = "[{ \"from\": \"ore\", \"to\": \"wood\" }]";
        JsonArray jsonArray = jsonValidator.getAsJsonObject(message);

        tradePhase.trade(player, jsonArray);

        // the trade should have succeeded.
        assertEquals(player.countResources(Resource.ORE), 0);
        assertEquals(player.countResources(Resource.WOOD), 1);
    }

    @Test
    public void testTradeWithHarbours() {
        game.setCurrentPlayer(player);
        Board board = game.getBoard();

        Edge harbourAllEdge = null;
        for (Edge edge : board.getEdges()) {
            if (edge.isHarbour() && edge.getHarbourType() == HarbourType.HARBOUR_ALL) {
                harbourAllEdge = edge;
                break;
            }
        }
        assertNotNull(harbourAllEdge);
        // get a node that is connected to this harbour_all edge
        Node harbourAllNode = board.getSurroundingNodes(harbourAllEdge).get(0);

        // set up a trade of 3 ore to wood
        player.addResources(Resource.ORE, 3);
        String message = "[{ \"from\": \"ore\", \"to\": \"wood\" }]";
        JsonArray jsonArray = new jsonValidator().getAsJsonObject(message);

        // the trade should initially not be legal
        assertFalse(tradePhase.tradeIsValid(player, jsonArray));

        // place a village on the harbour_all node. Now the same trade should be legal
        board.placeVillage(player, harbourAllNode);
        assertTrue(tradePhase.tradeIsValid(player, jsonArray));


        // the actual transaction should work as well
        tradePhase.trade(player, jsonArray);
        assertEquals(player.countResources(Resource.ORE), 0);
        assertEquals(player.countResources(Resource.WOOD), 1);
    }


    @Test
    public void testTradeWithHarbourOfResource() {
        game.setCurrentPlayer(player);
        Board board = game.getBoard();

        Edge harbourStoneEdge = null;
        for (Edge edge : board.getEdges()) {
            if (edge.isHarbour() && edge.getHarbourType() == HarbourType.HARBOUR_STONE) {
                harbourStoneEdge = edge;
                break;
            }
        }
        assertNotNull(harbourStoneEdge);
        // get a node that is connected to this harbour_all edge
        Node harbourStoneNode = board.getSurroundingNodes(harbourStoneEdge).get(0);

        // set up a trade of 2 stone to wood
        player.addResources(Resource.STONE, 2);
        String message = "[{ \"from\": \"stone\", \"to\": \"wood\" }]";
        JsonArray jsonArray = jsonValidator.getAsJsonObject(message);

        // the trade should initially not be legal
        assertFalse(tradePhase.tradeIsValid(player, jsonArray));
        assertFalse(player.canTradeWithBank());

        // place a village on the harbour_all node. Now the same trade should be legal
        board.placeVillage(player, harbourStoneNode);
        assertTrue(tradePhase.tradeIsValid(player, jsonArray));
        assertTrue(player.canTradeWithBank());

        // the actual transaction should work as well
        tradePhase.trade(player, jsonArray);
        assertEquals(player.countResources(Resource.STONE), 0);
        assertEquals(player.countResources(Resource.WOOD), 1);
    }

    @Test
    void itHandlesUserCommandsTest() {
        player.setMessageFromPlayer("[{ \"from\": \"stone\", \"to\": \"wood\" }]");
        player.addResources(Resource.STONE, 4);
        JsonArray jsonArray = tradePhase.getValidCommandFromUser(player);
        assertTrue(tradePhase.tradeIsValid(player, jsonArray));
    }

    @Test
    void itExecutes() {
        player.setMessageFromPlayer("[{ \"from\": \"stone\", \"to\": \"wood\" }]");
        player.addResources(Resource.STONE, 4);
        game.setCurrentPlayer(player);
        Phase phase = tradePhase.execute();
        assertEquals(Phase.BUILDING, phase);

        // the last response should be ok.
        assertEquals(Constants.OK.getCode(), game.getLastResponse().getCode());

    }
}


