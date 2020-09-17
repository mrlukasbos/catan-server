package game;

import board.Structure;
import utils.Helpers;

import java.util.ArrayList;
import java.util.HashMap;

public class Event {
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

    public Event(Game game, EventType type, Player player) {
        this.player = player;
        this.type = type;
        this.moveCount = game.getMoveCount();
    }

    public Event withResources(HashMap<Resource, Integer> resources) {
        this.resources = resources;
        return this;
    }

    public Event withStructures(ArrayList<Structure> structures) {
        this.structures = structures;
        return this;
    }

    public Event withGeneralMessage(String message) {
        this.message = message;
        return this;
    }

    @Override
    public String toString() {
        String structureString = Helpers.toJSONArray(structures, true);
        String resourcesString = Helpers.getJSONArrayFromHashMap(resources, "type", "value");

        String playerString = "";
        if (player != null) {
            playerString = "\"player\": " + player.getId() + ", ";
        }

        return "{" +
                "\"model\": \"event\", " +
                "\"attributes\": {" +
                "\"event_type\": \"" + type.toString() + "\", " +
                "\"move_count\": " + moveCount + ", " +
                "\"message\": \"" + message + "\", " +
                playerString +
                "\"resources\": " + resourcesString + ", " +
                "\"structures\": " + structureString +
                "}" +
                '}';
    }
}

