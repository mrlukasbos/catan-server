import java.util.ArrayList;

class Event {
    Player player = null;
    EventType type = EventType.GENERAL;
    ArrayList<Structure> structures = null;
    ArrayList<Resource> resources = null;

    Event(EventType type) {
        this.type = type;
    }

    Event(EventType type, Player player) {
        this.player = player;
        this.type = type;
    }

    Event withResources(ArrayList<Resource> resources) {
        this.resources = resources;
        return this;
    }

    Event withStructures(ArrayList<Structure> structures) {
        this.structures = structures;
        return this;
    }

    @Override
    public String toString() {
        return "";
    }
}

enum EventType {
    GENERAL,
    BUILD,
    GET_RESOURCES
}
