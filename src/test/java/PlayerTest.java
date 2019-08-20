import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PlayerTest {
    Player player = new Player(null,0, "tester");

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
        assertFalse(player.canBuild(Structure.SETTLEMENT));

        player.removeResources(Resource.ORE, 1);
        assertFalse(player.canBuildSomething());
        assertFalse(player.canBuild(Structure.CITY));

        player.addResources(Constants.VILLAGE_COSTS);
        assertTrue(player.canBuildSomething());
        assertFalse(player.canBuild(Structure.CITY));
        assertTrue(player.canBuild(Structure.SETTLEMENT));
        assertTrue(player.canBuild(Structure.STREET));
    }

    @Test
    void canTradeTest() {
        player.addResources(Constants.CITY_COSTS);
        player.addResources(Constants.VILLAGE_COSTS);
        assertFalse(player.canTradeWithBank());

        player.addResources(Resource.GRAIN, 1);
        assertTrue(player.canTradeWithBank());
    }

    @Test
    void getResourcesAsJsonStringTest() {
        player.addResources(Constants.STREET_COSTS);
        String jsonString =Player.getResourcesAsJSONString(player.getResources());
        JsonArray jsonArray = new jsonValidator().getJsonIfValid(player, jsonString);
        assertNotNull(jsonArray);
        for (JsonElement element : jsonArray) {
            String typename = element.getAsJsonObject().get("type").getAsString();
            int amount = element.getAsJsonObject().get("value").getAsInt();
            if (typename.equals("wood") || typename.equals("stone")) {
                assertEquals(amount, 1);
            } else {
                assertEquals(amount, 0);
            }
        }

    }
}
