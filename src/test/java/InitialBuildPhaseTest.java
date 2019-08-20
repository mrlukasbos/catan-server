import com.google.gson.JsonArray;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InitialBuildPhaseTest {
    private Interface iface = new Interface(10007);
    private Game game = new Game(iface);
    private InitialBuildPhase buildPhase = new InitialBuildPhase(game);
    private  Player player = new Player(0, "tester");

    @BeforeEach
    void beforeTest() {
        game.addPlayer(player);
        game.setCurrentPlayer(player);
        game.init();
    }

    @Test
    void buildingNothingIsIllegalTest() {
        String message = "[]";
        JsonArray jsonArray = new jsonValidator().getJsonIfValid(player, message);
        assertFalse(buildPhase.commandIsValid(player, jsonArray));
        assertEquals(game.getLastResponse().getCode(), Constants.NOTAVILLAGEANDSTREETERROR.getCode());
    }

    @Test
    void buildingTwoStreetsIsIllegalTest() {
        String message = "[{ \"structure\": \"street\", \"location\": \"([3,1],[3,2])\" }, { \"structure\": \"street\", \"location\": \"([2,2],[3,2])\" }]";
        JsonArray jsonArray = new jsonValidator().getJsonIfValid(player, message);
        assertFalse(buildPhase.commandIsValid(player, jsonArray));
        assertEquals(game.getLastResponse().getCode(), Constants.NOTAVILLAGEANDSTREETERROR.getCode());
    }


    @Test
    void buildingDisconnectedStreetAndVillageIsIllegalTest() {
        String message = " [{ \"structure\": \"street\", \"location\": \"([4,1],[4,2])\" }, { \"structure\": \"village\", \"location\": \"([2,2],[3,1],[3,2])\" }]";
        JsonArray jsonArray = new jsonValidator().getJsonIfValid(player, message);
        assertFalse(buildPhase.commandIsValid(player, jsonArray));
        assertEquals(game.getLastResponse().getCode(), Constants.STRUCTURENOTCONNECTEDERROR.getCode());
    }

    @Test
    void buildingConnectedStreetAndVillageIsLegalTest() {
        String message = " [{ \"structure\": \"street\", \"location\": \"([3,1],[3,2])\" }, { \"structure\": \"village\", \"location\": \"([2,2],[3,1],[3,2])\" }]";
        JsonArray jsonArray = new jsonValidator().getJsonIfValid(player, message);
        assertTrue(buildPhase.commandIsValid(player, jsonArray));
    }

    @Test
    void buildingVillagesOnTopOfEachOtherIsIllegalTest() {
        game.getBoard().placeStreet(player, game.getBoard().getEdge("([3,1],[3,2])"));

        String message = "[{ \"structure\": \"street\", \"location\": \"([3,1],[3,2])\" }, { \"structure\": \"village\", \"location\": \"([2,2],[3,1],[3,2])\" }]";
        JsonArray jsonArray = new jsonValidator().getJsonIfValid(player, message);
        assertFalse(buildPhase.commandIsValid(player, jsonArray));
        assertEquals(game.getLastResponse().getCode(), Constants.STRUCTUREALREADYEXISTSERROR.getCode());
    }

    @Test
    void buildingStreetsOnTopOfEachOtherIsIllegalTest() {
        game.getBoard().placeVillage(player, game.getBoard().getNode("([2,2],[3,1],[3,2])"));

        String message = "[{ \"structure\": \"street\", \"location\": \"([3,1],[3,2])\" }, { \"structure\": \"village\", \"location\": \"([2,2],[3,1],[3,2])\" }]";
        JsonArray jsonArray = new jsonValidator().getJsonIfValid(player, message);
        assertFalse(buildPhase.commandIsValid(player, jsonArray));
        assertEquals(game.getLastResponse().getCode(), Constants.STRUCTUREALREADYEXISTSERROR.getCode());
    }

    @Test
    void buildingVillagesTooCloseIsIllegalTest() {
        game.getBoard().placeVillage(player, game.getBoard().getNode("([2,1],[2,2],[3,1])"));

        String message = "[{ \"structure\": \"street\", \"location\": \"([3,1],[3,2])\" }, { \"structure\": \"village\", \"location\": \"([2,2],[3,1],[3,2])\" }]";
        JsonArray jsonArray = new jsonValidator().getJsonIfValid(player, message);
        assertFalse(buildPhase.commandIsValid(player, jsonArray));
        assertEquals(game.getLastResponse().getCode(), Constants.STRUCTURETOOCLOSETOOTHERSTRUCTUREERROR.getCode());
    }

    @Test
    void itProceedsToTheNextPhase() {
        game.getBoard().placeStreet(player, game.getBoard().getEdge("([3,1],[3,2])"));
        game.getBoard().placeStreet(player, game.getBoard().getEdge("([2,2],[3,2])"));
        game.getBoard().placeVillage(player, game.getBoard().getNode("([2,2],[3,1],[3,2])"));

        assertFalse(buildPhase.ShouldProceedToNextPhase());
        assertEquals(buildPhase.getNextPhase(), Phase.INITIAL_BUILDING);

        game.getBoard().placeVillage(player, game.getBoard().getNode("([2,3],[2,4],[3,3])"));

        assertTrue(buildPhase.ShouldProceedToNextPhase());
        assertEquals(buildPhase.getNextPhase(), Phase.THROW_DICE);
    }

    @Test
    void itChangesPlayerTest() {
        Player player2 = new Player(1, "tester2");
        Player player3 = new Player(2, "tester3");

        game.addPlayer(player2);
        game.addPlayer(player3);
        game.setCurrentPlayer(player);

        game.getBoard().placeVillage(player, game.getBoard().getNode("([2,2],[3,1],[3,2])"));
        game.getBoard().placeVillage(player, game.getBoard().getNode("([3,3],[3,4],[4,3])"));

        buildPhase.changePlayer();
        assertEquals(game.getCurrentPlayer().getId(), player2.getId()); // should be next player

        game.getBoard().placeVillage(player, game.getBoard().getNode("([1,4],[2,4],[2,5])"));
        game.getBoard().placeVillage(player, game.getBoard().getNode("([2,3],[2,4],[3,3])"));

        buildPhase.changePlayer();
        assertEquals(game.getCurrentPlayer().getId(), player.getId()); // should be previous player

        game.getBoard().placeVillage(player, game.getBoard().getNode("([1,2],[2,1],[2,2])"));
        game.getBoard().placeVillage(player, game.getBoard().getNode("([4,3],[4,4],[5,3])"));

        buildPhase.changePlayer();
        assertEquals(game.getCurrentPlayer().getId(), player.getId()); // should be same player
    }

}
