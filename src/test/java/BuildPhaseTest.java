import com.google.gson.JsonArray;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BuildPhaseTest {
    private Interface iface = new Interface(10007);
    private Game game = new Game(iface);
    private BuildPhase buildPhase = new BuildPhase(game);
    private  Player player = new Player(0, "tester");
    private  Player player2 = new Player(1, "tester1");

    @BeforeEach
    void beforeTest() {
        game.addPlayer(player);
        game.setCurrentPlayer(player);
        game.init();
    }

    @Test
    void itShouldReturnFalseIfEmptyInputTest() {
        assertFalse(buildPhase.commandIsValid(player, null));
        assertEquals(game.getLastResponse().getCode(), Constants.MALFORMEDJSONERROR.getCode());
    }

    @Test
    void phaseNameShouldBeCorrectTest() {
        assertEquals(buildPhase.getPhaseType(), Phase.BUILDING);
    }

    @Test
    void testBuildingNothing() {
        String message = "[]";
        JsonArray jsonArray = new jsonValidator().getJsonIfValid(player, message);
        assertTrue(buildPhase.commandIsValid(player, jsonArray));
    }

    @Test
    void legalStreetTest() {
        game.getBoard().placeStreet(player, game.getBoard().getEdge("([3,2],[3,3])"));

        player.addResources(Constants.STREET_COSTS);

        String message = "[{ \"structure\": \"street\", \"location\": \"([2,2],[3,2])\" }]";
        JsonArray jsonArray = new jsonValidator().getJsonIfValid(player, message);
        assertTrue(buildPhase.commandIsValid(player, jsonArray));
    }

    @Test
    void streetMustBeConnectedTest() {
        game.getBoard().placeStreet(player2, game.getBoard().getEdge("([2,2],[3,2])"));

        player.addResources(Constants.STREET_COSTS);

        String message = "[{ \"structure\": \"street\", \"location\": \"([3,1],[3,2])\" }]";
        JsonArray jsonArray = new jsonValidator().getJsonIfValid(player, message);
        assertFalse(buildPhase.commandIsValid(player, jsonArray));
        assertEquals(game.getLastResponse().getCode(), Constants.STRUCTURENOTCONNECTEDERROR.getCode());
    }

    @Test
    void streetsMayNotOverlapTest() {
        game.getBoard().placeStreet(player, game.getBoard().getEdge("([3,1],[3,2])"));
        game.getBoard().placeStreet(player, game.getBoard().getEdge("([2,2],[3,2])"));

        player.addResources(Constants.STREET_COSTS);

        String message = "[{ \"structure\": \"street\", \"location\": \"([3,1],[3,2])\" }]";
        JsonArray jsonArray = new jsonValidator().getJsonIfValid(player, message);
        assertFalse(buildPhase.commandIsValid(player, jsonArray));
        assertEquals(game.getLastResponse().getCode(), Constants.STRUCTUREALREADYEXISTSERROR.getCode());
    }

    @Test
    void streetsCanBeConnectedUsingStreetsInSameCommandTest() {
        game.getBoard().placeStreet(player, game.getBoard().getEdge("([3,2],[3,3])"));

        player.addResources(Constants.STREET_COSTS);
        player.addResources(Constants.STREET_COSTS);

        String message = "[{ \"structure\": \"street\", \"location\": \"([3,1],[3,2])\" }, { \"structure\": \"street\", \"location\": \"([2,2],[3,2])\" }]";
        JsonArray jsonArray = new jsonValidator().getJsonIfValid(player, message);
        assertTrue(buildPhase.commandIsValid(player, jsonArray));
    }

    @Test
    void streetsCanNotBeBuildWithoutResourcesTest() {
        game.getBoard().placeStreet(player, game.getBoard().getEdge("([3,2],[3,3])"));

        player.addResources(Resource.WOOD, 2);
        player.addResources(Resource.STONE, 1);

        String message = "[{ \"structure\": \"street\", \"location\": \"([3,1],[3,2])\" }, { \"structure\": \"street\", \"location\": \"([2,2],[3,2])\" }]";
        JsonArray jsonArray = new jsonValidator().getJsonIfValid(player, message);
        assertFalse(buildPhase.commandIsValid(player, jsonArray));
        assertEquals(game.getLastResponse().getCode(), Constants.NOTENOUGHRESOURCESERROR.getCode());
    }

    @Test
    void legalVillageTest() {
        game.getBoard().placeStreet(player, game.getBoard().getEdge("([3,2],[3,3])"));

        player.addResources(Constants.STREET_COSTS);
        player.addResources(Constants.VILLAGE_COSTS);

        String message = " [{ \"structure\": \"street\", \"location\": \"([2,2],[3,2])\" }, { \"structure\": \"village\", \"location\": \"([2,2],[3,1],[3,2])\" }]";
        JsonArray jsonArray = new jsonValidator().getJsonIfValid(player, message);
        assertTrue(buildPhase.commandIsValid(player, jsonArray));
    }

    @Test
    void villageMustBeConnectedTest() {
        game.getBoard().placeStreet(player, game.getBoard().getEdge("([3,2],[3,3])"));

        player.addResources(Constants.VILLAGE_COSTS);

        String message = "[{ \"structure\": \"village\", \"location\": \"([1,2],[2,1],[2,2])\" }]";
        JsonArray jsonArray = new jsonValidator().getJsonIfValid(player, message);
        assertFalse(buildPhase.commandIsValid(player, jsonArray));
        assertEquals(game.getLastResponse().getCode(), Constants.STRUCTURENOTCONNECTEDERROR.getCode());
    }



    @Test
    void villageCanNotBePlacedWithoutResourcesTest() {
        game.getBoard().placeStreet(player, game.getBoard().getEdge("([3,2],[3,3])"));

        player.addResources(Constants.VILLAGE_COSTS);

        String message = " [{ \"structure\": \"street\", \"location\": \"([2,2],[3,2])\" }, { \"structure\": \"village\", \"location\": \"([2,2],[3,1],[3,2])\" }]";
        JsonArray jsonArray = new jsonValidator().getJsonIfValid(player, message);
        assertFalse(buildPhase.commandIsValid(player, jsonArray));
        assertEquals(game.getLastResponse().getCode(), Constants.NOTENOUGHRESOURCESERROR.getCode());
    }

    @Test
    void villageCanNotBeOneEdgeNextToOtherVillageTest() {
        game.getBoard().placeStreet(player, game.getBoard().getEdge("([3,2],[3,3])"));
        game.getBoard().placeStreet(player, game.getBoard().getEdge("([2,2],[3,2])"));
        game.getBoard().placeCity(player, game.getBoard().getNode("([2,2],[3,1],[3,2])"));

        player.addResources(Constants.VILLAGE_COSTS);

        String message = "[{ \"structure\": \"village\", \"location\": \"([2,2],[3,2],[3,3])\" }]";
        JsonArray jsonArray = new jsonValidator().getJsonIfValid(player, message);
        assertFalse(buildPhase.commandIsValid(player, jsonArray));
        assertEquals(game.getLastResponse().getCode(), Constants.STRUCTURETOOCLOSETOOTHERSTRUCTUREERROR.getCode());
    }

    @Test
    void villageCanNotBeOneEdgeNextToOtherVillageInSameCommandTest() {
        game.getBoard().placeStreet(player, game.getBoard().getEdge("([3,2],[3,3])"));
        game.getBoard().placeStreet(player, game.getBoard().getEdge("([2,2],[3,2])"));

        player.addResources(Constants.VILLAGE_COSTS);
        player.addResources(Constants.VILLAGE_COSTS);

        String message = "[{ \"structure\": \"village\", \"location\": \"([2,2],[3,2],[3,3])\" }, { \"structure\": \"village\", \"location\": \"([2,2],[3,1],[3,2])\" }]";
        JsonArray jsonArray = new jsonValidator().getJsonIfValid(player, message);
        assertFalse(buildPhase.commandIsValid(player, jsonArray));
        assertEquals(game.getLastResponse().getCode(), Constants.STRUCTURETOOCLOSETOOTHERSTRUCTUREERROR.getCode());
    }

    @Test
    void villageCanBeTwoNextToOtherTest() {
        game.getBoard().placeStreet(player, game.getBoard().getEdge("([3,2],[3,3])"));
        game.getBoard().placeStreet(player, game.getBoard().getEdge("([2,2],[3,2])"));
        game.getBoard().placeVillage(player, game.getBoard().getNode("([2,2],[3,1],[3,2])"));

        player.addResources(Constants.VILLAGE_COSTS);

        String message = "[{ \"structure\": \"village\", \"location\": \"([3,2],[3,3],[4,3])\" }]";
        JsonArray jsonArray = new jsonValidator().getJsonIfValid(player, message);
        assertTrue(buildPhase.commandIsValid(player, jsonArray));
    }

    @Test
    void legalCityTest() {
        game.getBoard().placeStreet(player, game.getBoard().getEdge("([3,2],[3,3])"));
        game.getBoard().placeStreet(player, game.getBoard().getEdge("([2,2],[3,2])"));
        game.getBoard().placeVillage(player, game.getBoard().getNode("([2,2],[3,1],[3,2])"));

        player.addResources(Constants.CITY_COSTS);

        String message = "[{ \"structure\": \"city\", \"location\": \"([2,2],[3,1],[3,2])\" }]";
        JsonArray jsonArray = new jsonValidator().getJsonIfValid(player, message);
        assertTrue(buildPhase.commandIsValid(player, jsonArray));
    }

    @Test
    void cityCanNotBeBuildOnOtherPlayerVillagesTest() {
        game.getBoard().placeStreet(player, game.getBoard().getEdge("([3,2],[3,3])"));
        game.getBoard().placeStreet(player, game.getBoard().getEdge("([2,2],[3,2])"));
        game.getBoard().placeVillage(player2, game.getBoard().getNode("([2,2],[3,1],[3,2])"));

        player.addResources(Constants.CITY_COSTS);

        String message = "[{ \"structure\": \"city\", \"location\": \"([2,2],[3,1],[3,2])\" }]";
        JsonArray jsonArray = new jsonValidator().getJsonIfValid(player, message);
        assertFalse(buildPhase.commandIsValid(player, jsonArray));
        assertEquals(game.getLastResponse().getCode(), Constants.CITYNOTBUILTONPLAYERSVILLAGEERROR.getCode());
    }

    @Test
    void cityCanNotBeBuildWithoutVillageTest() {
        game.getBoard().placeStreet(player, game.getBoard().getEdge("([3,2],[3,3])"));
        game.getBoard().placeStreet(player, game.getBoard().getEdge("([2,2],[3,2])"));
        game.getBoard().placeCity(player, game.getBoard().getNode("([2,2],[3,1],[3,2])"));

        player.addResources(Constants.CITY_COSTS);

        String message = "[{ \"structure\": \"city\", \"location\": \"([2,2],[3,1],[3,2])\" }]";
        JsonArray jsonArray = new jsonValidator().getJsonIfValid(player, message);
        assertFalse(buildPhase.commandIsValid(player, jsonArray));
        assertEquals(game.getLastResponse().getCode(), Constants.CITYNOTBUILTONPLAYERSVILLAGEERROR.getCode());
    }


    @Test
    void cityCanBeBuildWithVillageInOneCommandTest() {
        game.getBoard().placeStreet(player, game.getBoard().getEdge("([3,2],[3,3])"));
        game.getBoard().placeStreet(player, game.getBoard().getEdge("([2,2],[3,2])"));

        player.addResources(Constants.VILLAGE_COSTS);
        player.addResources(Constants.CITY_COSTS);

        String message = "[{ \"structure\": \"city\", \"location\": \"([2,2],[3,1],[3,2])\" }, { \"structure\": \"village\", \"location\": \"([2,2],[3,1],[3,2])\" }]";
        JsonArray jsonArray = new jsonValidator().getJsonIfValid(player, message);
        assertTrue(buildPhase.commandIsValid(player, jsonArray));
    }

    @Test
    void cityCanNotBePlacedWithoutResourcesTest() {
        game.getBoard().placeStreet(player, game.getBoard().getEdge("([3,2],[3,3])"));
        game.getBoard().placeStreet(player, game.getBoard().getEdge("([2,2],[3,2])"));

        player.addResources(Constants.VILLAGE_COSTS);

        String message = "[{ \"structure\": \"city\", \"location\": \"([2,2],[3,1],[3,2])\" }]";
        JsonArray jsonArray = new jsonValidator().getJsonIfValid(player, message);
        assertFalse(buildPhase.commandIsValid(player, jsonArray));
        assertEquals(game.getLastResponse().getCode(), Constants.NOTENOUGHRESOURCESERROR.getCode());
    }


    @Test
    void buildingStructuresShouldChangeBoardTest() {
        game.getBoard().placeStreet(player, game.getBoard().getEdge("([3,2],[3,3])"));

        player.addResources(Constants.STREET_COSTS);
        player.addResources(Constants.VILLAGE_COSTS);
        player.addResources(Constants.CITY_COSTS);

        String message = "[{ \"structure\": \"street\", \"location\": \"([2,2],[3,2])\" }, { \"structure\": \"city\", \"location\": \"([2,2],[3,1],[3,2])\" }, { \"structure\": \"village\", \"location\": \"([2,2],[3,1],[3,2])\" }]";
        JsonArray jsonArray = new jsonValidator().getJsonIfValid(player, message);
        assertTrue(buildPhase.commandIsValid(player, jsonArray));

        assertEquals(game.getBoard().getStreetsFromPlayer(player).size(), 1);
        assertEquals(game.getBoard().getAllStructures().size(), 0);

        buildPhase.buildStructures(player, jsonArray);

        assertEquals(game.getBoard().getStreetsFromPlayer(player).size(), 2);
        assertEquals(game.getBoard().getAllStructures().size(), 1);
        assertEquals(game.getBoard().getAllStructures().get(0), Structure.CITY);

        // the player should be out of resources
        assertEquals(player.countResources(), 0);

    }
}
