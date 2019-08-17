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
        String structureString = "[";

        if (structures.size() == 0) {
            structureString = "[]";
        } else {
            for (Structure structure : structures) {
                structureString = structureString.concat("\"" + player.structureToString(structure) + "\",");
            }
            structureString = structureString.substring(0, structureString.length() - 1);
            structureString = structureString.concat("]");
        }

        String playerString = "null";
        if (player != null) {
            playerString = player.toString();
        }

        return "{" +
                "\"model\": \"event\", " +
                "\"type\": \"" + eventTypeToString(type) + "\", " +
                "\"attributes\": {" +
                "\"move\": " + moveCount + ", " +
                "\"message\": \"" + message + "\", " +
                "\"player\": " + playerString + ", " +
                "\"resources\": " + Player.getResourcesAsJSONString(resources) + ", " +
                "\"structures\": " + structureString +
                "}" +
                '}';
    }

    public String toReadableString() {
        switch (type) {
            case GENERAL: {
                return "";
            }
            case BUILD: {
                StringBuilder strBuilder = new StringBuilder(player.getName() + " built a ");
                for (int i = 0; i < structures.size(); i++) {
                    strBuilder.append(Player.structureToString(structures.get(i)));
                    if (i < structures.size() - 2) {
                        strBuilder.append(", ");
                    } else if (i == structures.size() -2) {
                        strBuilder.append(" and a ");
                    }
                }
                return strBuilder.toString();
            }
            case GET_RESOURCES: {
                StringBuilder strBuilder = new StringBuilder(player.getName() + " receives ");

                int index = 0;
                for (HashMap.Entry<Resource, Integer> entry : resources.entrySet()) {
                    strBuilder.append(entry.getValue()).append(" ").append(Player.resourceToString(entry.getKey())).append(", ");
                    if (index < resources.size() - 2) {
                        strBuilder.append(", ");
                    } else if (index  == resources.size() -2) {
                        strBuilder.append(" and a ");
                    }
                    index++;
                }
                return strBuilder.toString();
            }
        }

        return "";
    }

    private String eventTypeToString(EventType type) {
        switch (type) {
            case GET_RESOURCES: return "GET_RESOURCES";
            case GENERAL: return "GENERAL";
            case BUILD: return "BUIlD";
            default: return "";
        }
    }
}

enum EventType {
    GENERAL,
    BUILD,
    GET_RESOURCES
}
