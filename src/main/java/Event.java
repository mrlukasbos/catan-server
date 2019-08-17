import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class Event {
    private Player player = null;
    private EventType type;
    private ArrayList<Structure> structures = null;
    private HashMap<Resource, Integer> resources = null;

    Event(EventType type) {
        this.type = type;
    }

    Event(EventType type, Player player) {
        this.player = player;
        this.type = type;
    }

    Event withResources(HashMap<Resource, Integer> resources) {
        this.resources = resources;
        return this;
    }

    Event withStructures(ArrayList<Structure> structures) {
        this.structures = structures;
        return this;
    }

    @Override
    public String toString() {
        switch (type) {
            case GENERAL: {
                return "";
            }
            case BUILD: {
                StringBuilder strBuilder = new StringBuilder(player.getName() + " built a ");
                for (int i = 0; i < structures.size(); i++) {
                    strBuilder.append(player.structureToString(structures.get(i)));
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
                    strBuilder.append(entry.getValue()).append(" ").append(player.resourceToString(entry.getKey())).append(", ");
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
}

enum EventType {
    GENERAL,
    BUILD,
    GET_RESOURCES
}
