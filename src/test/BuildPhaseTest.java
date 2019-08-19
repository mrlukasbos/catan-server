import com.google.gson.JsonArray;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BuildPhaseTest {
    Interface iface = new Interface(10007);
    Game game = new Game(iface);
    BuildPhase buildPhase = new BuildPhase(game);
    Player player = new Player(0, "tester");

    @Test
    public void testBuildingNothing() {
        game.addPlayer(player);
        game.startGame();

        String message = "[]";
        JsonArray jsonArray = new jsonValidator().getJsonIfValid(player, message);
        assertTrue(buildPhase.commandIsValid(player, jsonArray));
    }

    @Test
    public void testBuildingValidStreet() {
        game.addPlayer(player);
        game.setCurrentPlayer(player);
        game.getBoard().placeStreet(player, game.getBoard().getEdge("([3,2],[3,3])"));
        player.addResources(Resource.WOOD, 1);
        player.addResources(Resource.STONE, 1);

        String message = "[{ \"structure\": \"street\", \"location\": \"([2,2],[3,2])\" }]";

        JsonArray jsonArray = new jsonValidator().getJsonIfValid(player, message);
        assertTrue(buildPhase.commandIsValid(player, jsonArray));
    }

    @Test
    public void testBuildingUnconnectedStreet() {
        game.addPlayer(player);
        game.setCurrentPlayer(player);

        player.addResources(Resource.WOOD, 1);
        player.addResources(Resource.STONE, 1);

        String message = "[{ \"structure\": \"street\", \"location\": \"([3,1],[3,2])\" }]";
        JsonArray jsonArray = new jsonValidator().getJsonIfValid(player, message);
        assertFalse(buildPhase.commandIsValid(player, jsonArray));
        assertEquals(game.getLastResponse().getCode(), Constants.STRUCTURENOTCONNECTEDERROR.getCode());
    }

    @Test
    public void testBuildingOverlappingStreet() {
        game.addPlayer(player);
        game.setCurrentPlayer(player);
        game.getBoard().placeStreet(player, game.getBoard().getEdge("([3,1],[3,2])"));
        game.getBoard().placeStreet(player, game.getBoard().getEdge("([2,2],[3,2])"));

        player.addResources(Resource.WOOD, 1);
        player.addResources(Resource.STONE, 1);

        String message = "[{ \"structure\": \"street\", \"location\": \"([3,1],[3,2])\" }]";
        JsonArray jsonArray = new jsonValidator().getJsonIfValid(player, message);
        assertFalse(buildPhase.commandIsValid(player, jsonArray));
        assertEquals(game.getLastResponse().getCode(), Constants.STRUCTUREALREADYEXISTSERROR.getCode());
    }



    @Test
    public void testBuildingMultipleStreetsAtOnce() {
        game.addPlayer(player);
        game.setCurrentPlayer(player);
        game.getBoard().placeStreet(player, game.getBoard().getEdge("([3,2],[3,3])"));

        player.addResources(Resource.WOOD, 2);
        player.addResources(Resource.STONE, 2);

        String message = "[{ \"structure\": \"street\", \"location\": \"([3,1],[3,2])\" }, { \"structure\": \"street\", \"location\": \"([2,2],[3,2])\" }]";

        JsonArray jsonArray = new jsonValidator().getJsonIfValid(player, message);
        assertTrue(buildPhase.commandIsValid(player, jsonArray));
    }

    @Test
    public void testBuildingWithWrongResources() {
        game.addPlayer(player);
        game.setCurrentPlayer(player);
        game.getBoard().placeStreet(player, game.getBoard().getEdge("([3,2],[3,3])"));

        player.addResources(Resource.WOOD, 2);
        player.addResources(Resource.STONE, 1);

        String message = "[{ \"structure\": \"street\", \"location\": \"([3,1],[3,2])\" }, { \"structure\": \"street\", \"location\": \"([2,2],[3,2])\" }]";
        JsonArray jsonArray = new jsonValidator().getJsonIfValid(player, message);
        assertFalse(buildPhase.commandIsValid(player, jsonArray));
        assertEquals(game.getLastResponse().getCode(), Constants.NOTENOUGHRESOURCESERROR.getCode());
    }
}
