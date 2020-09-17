import board.Structure;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import communication.WebSocketConnectionServer;
import game.Game;
import game.Player;
import game.Resource;
import org.junit.jupiter.api.Test;
import utils.Constants;
import utils.Helpers;
import utils.jsonValidator;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PlayerTest {
    WebSocketConnectionServer iface = new WebSocketConnectionServer(10007);
    Game game = new Game(iface);
    Player player = new PlayerStub(game,0, "tester");

    @Test
    void itAddsAndRemovesRecourcesTest() {

        // add resources
        player.addResources(Resource.WOOD, 3);
        player.addResources(Resource.GRAIN, 2);
        player.addResources(Constants.CITY_COSTS);

        assertEquals(player.countResources(), 10);
        assertEquals(player.countResources(Resource.GRAIN), 4);
        assertEquals(player.countResources(Resource.WOOD), 3);

        // remove resources
        player.removeResources(Resource.GRAIN, 2);
        player.addResources(Resource.WOOD, -2);

        assertEquals(player.countResources(), 6);
        assertEquals(player.countResources(Resource.GRAIN), 2);
        assertEquals(player.countResources(Resource.WOOD), 1);

        player.pay(Structure.CITY);

        assertEquals(player.countResources(), 1);
        assertEquals(player.countResources(Resource.GRAIN), 0);
        assertEquals(player.countResources(Resource.WOOD), 1);

        player.removeResources();

        assertEquals(player.countResources(), 0);
        assertEquals(player.countResources(Resource.GRAIN), 0);
        assertEquals(player.countResources(Resource.WOOD), 0);

        // check if it does not go below 0
        player.removeResources(Resource.GRAIN, 2);
        assertEquals(player.countResources(Resource.GRAIN), 0);
        player.addResources(Resource.GRAIN, -2);
        assertEquals(player.countResources(Resource.GRAIN), 0);
    }


    @Test
    void canBuildTest() {
        player.addResources(Constants.CITY_COSTS);
        player.removeResources(Resource.WOOD, 3);
        assertTrue(player.canBuild(Structure.CITY));
        assertTrue(player.canBuildSomething());
        assertFalse(player.canBuild(Structure.VILLAGE));
        assertFalse(player.canBuild(Structure.DEVELOPMENT_CARD));

        player.removeResources(Resource.ORE, 1);
        assertFalse(player.canBuildSomething());
        assertFalse(player.canBuild(Structure.CITY));

        player.addResources(Constants.VILLAGE_COSTS);
        assertTrue(player.canBuildSomething());
        assertFalse(player.canBuild(Structure.CITY));
        assertTrue(player.canBuild(Structure.VILLAGE));
        assertTrue(player.canBuild(Structure.STREET));
    }

    @Test
    void canTradeTest() {
        // the behaviour of this method with harbours is tested in the TradePhasesTest
        player.addResources(Constants.CITY_COSTS);
        player.addResources(Constants.VILLAGE_COSTS);
        assertFalse(player.canTradeWithBank());

        player.addResources(Resource.GRAIN, 1);
        assertTrue(player.canTradeWithBank());
    }

    @Test
    void getResourcesAsJsonStringTest() {
        player.addResources(Constants.STREET_COSTS);
        String jsonString = Helpers.getJSONArrayFromHashMap(player.getResources(), "type", "value");
        JsonArray jsonArray = jsonValidator.getAsJsonArray(jsonString);
        assertNotNull(jsonArray);
        for (JsonElement element : jsonArray) {
            String typename = element.getAsJsonObject().get("type").getAsString();
            int amount = element.getAsJsonObject().get("value").getAsInt();
            if (typename.equals("WOOD") || typename.equals("STONE")) {
                assertEquals(amount, 1);
            } else {
                assertEquals(amount, 0);
            }
        }

    }
}
