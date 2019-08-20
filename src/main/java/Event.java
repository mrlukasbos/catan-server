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
        String structureString = getStructureString();

        String playerString = "null";
        if (player != null) {
            playerString = player.toString();
        }

        return "{" +
                "\"model\": \"event\", " +
                "\"type\": \"" + eventTypeToString(type) + "\", " +
                "\"attributes\": {" +
                "\"moveCount\": " + moveCount + ", " +
                "\"message\": \"" + message + "\", " +
                "\"player\": " + playerString + ", " +
                "\"resources\": " + Player.getResourcesAsJSONString(resources) + ", " +
                "\"structures\": " + structureString +
                "}" +
                '}';
    }

    private String getStructureString() {
        String structureString = "[";

        if (structures.size() == 0) {
            structureString = "[]";
        } else {
            for (Structure structure : structures) {
                structureString = structureString.concat("\"" + Player.structureToString(structure) + "\",");
            }
            structureString = structureString.substring(0, structureString.length() - 1);
            structureString = structureString.concat("]");
        }
        return structureString;
    }

    private String eventTypeToString(EventType type) {
        switch (type) {
            case GET_RESOURCES: return "GET_RESOURCES";
            case GENERAL: return "GENERAL";
            case TRADE: return "TRADING";
            case BUILD: return "BUIlD";
            default: return "";
        }
    }
}

enum EventType {
    GENERAL,
    BUILD,
    TRADE,
    GET_RESOURCES
}
