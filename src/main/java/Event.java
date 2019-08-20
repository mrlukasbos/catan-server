import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class Event {
    private Player player = null;
    private EventType type;
    private ArrayList<Structure> structures = new ArrayList<>();
    private HashMap<Resource, Integer> resources = new HashMap<>();
    private String message = "";
    private int moveCount;

    Event(Game game, EventType type) {
        this.type = type;
        this.moveCount = game.getMoveCount();
    }

    Event(Game game, EventType type, Player player) {
        this.player = player;
        this.type = type;
        this.moveCount = game.getMoveCount();
    }

    Event withResources(HashMap<Resource, Integer> resources) {
        this.resources = resources;
        return this;
    }

    Event withStructures(ArrayList<Structure> structures) {
        this.structures = structures;
        return this;
    }

    Event withGeneralMessage(String message) {
        this.message = message;
        return this;
    }

    @Override
    public String toString() {
        String structureString = Helpers.toJSONArray(structures, true,",");
        String resourcesString = Helpers.getJSONArrayFromHashMap(resources, "type", "value");

        String playerString = "null";
        if (player != null) {
            playerString = player.toString();
        }

        return "{" +
                "\"model\": \"event\", " +
                "\"type\": \"" + type.toString() + "\", " +
                "\"attributes\": {" +
                "\"moveCount\": " + moveCount + ", " +
                "\"message\": \"" + message + "\", " +
                "\"player\": " + playerString + ", " +
                "\"resources\": " + resourcesString + ", " +
                "\"structures\": " + structureString +
                "}" +
                '}';
    }
}

enum EventType {
    GENERAL,
    BUILD,
    TRADE,
    GET_RESOURCES
}
