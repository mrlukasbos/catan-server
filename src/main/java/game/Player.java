package game;

import board.DevelopmentCard;
import board.Structure;
import communication.Connection;
import utils.Constants;
import utils.Helpers;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class Player {
    private String name;
    private int id;
    private HashMap<Resource, Integer> resources = new HashMap<>();
    private HashMap<DevelopmentCard, Integer> developmentCards = new HashMap<>();
    private HashMap<DevelopmentCard, Integer> usedDevelopmentCards = new HashMap<>();
    private String color;
    private Game game;
    private Connection connection;
    private String bufferedReply = "";

    public synchronized void setBufferedReply(String bufferedReply) {
        this.bufferedReply = bufferedReply;
        notify();
    }

    public void send(String str) {
        connection.send(str);
    }

    public String listen() {
        while (bufferedReply.trim().length() == 0 && game.isRunning()) {
            try {
                wait();
            } catch (Exception e) {}
        }  // block until there is a reply
        print("new buffered reply: " +  bufferedReply);

        String reply = bufferedReply;
        bufferedReply = "";
        return reply;
    }

    void stop() {
        connection.close();
    }

    public Connection getConnection() {
        return connection;
    }

    public Player(Connection connection, Game game, int id, String name) {
        this.connection = connection;
        this.id = id;
        this.name = name;
        this.game = game;
        this.color = String.format("#%06x", new Random().nextInt(0xffffff + 1));

        resources.put(Resource.GRAIN, 0);
        resources.put(Resource.ORE, 0);
        resources.put(Resource.STONE, 0);
        resources.put(Resource.WOOL, 0);
        resources.put(Resource.WOOD, 0);
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public HashMap<Resource, Integer> getResources() {
        return resources;
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    public int getId() {
        return id;
    }

    // count the occurence of a specific resource for the player
    public int countResources(Resource resourceToCount) {
        return resources.getOrDefault(resourceToCount, 0);
    }

    // count the occurence of all resources together
    public int countResources() {
        int count = 0;
        for (Resource res : Constants.ALL_RESOURCES) {
            count += countResources(res);
        }
        return count;
    }

    // add resources to the resources of the player
    public void addResources(Resource resource, int amount) {
        if (resource != Resource.NONE) {
            resources.replace(resource, Math.max(0, resources.get(resource) + amount));
        }
    }

    public void addResources(Map<Resource, Integer> newResources) {
        for (Map.Entry<Resource, Integer> entry : newResources.entrySet()) {
            addResources(entry.getKey(), entry.getValue());
        }
    }

    public void removeResources(Resource resource, int amount) {
        addResources(resource, -amount);
    }

    void removeResources(Map<Resource, Integer> resourcesToRemove) {
        for (Map.Entry<Resource, Integer> entry : resourcesToRemove.entrySet()) {
            removeResources(entry.getKey(), entry.getValue());
        }
    }

    public void removeResources() {
        for (Resource resource : Constants.ALL_RESOURCES) {
            resources.replace(resource, 0);
        }
    }

    @java.lang.Override
    public java.lang.String toString() {
        String resourcesString = Helpers.getJSONArrayFromHashMap(resources, "type", "value");
        String usedDevelopmentCardsString = Helpers.getJSONArrayFromHashMap(usedDevelopmentCards, "type", "value");

        return "{" +
                "\"model\": \"player\", " +
                "\"attributes\": {" +
                "\"id\": " + getId() + ", " +
                "\"color\": \"" + getColor() + "\", " +
                "\"name\": \"" + getName() + "\", " +
                "\"resources\": " + resourcesString + ", " +
                "\"used_development_cards\": " + usedDevelopmentCardsString + ", " +
                "\"unused_development_cards\": " + amountOfUnusedDevelopmentCards() +
                "}" +
                "}";
    }

    private void print(String msg) {
        System.out.println("[game.Player] " + msg);
    }

    public boolean canTradeWithBank() {
        for (Map.Entry<Resource, Integer> resource : resources.entrySet()) {
            if (resource.getValue() >= game.getRequiredAmountOfCardsToTrade(this, resource.getKey())) return true;
        }
        return false;
    }

    public boolean canBuild(Structure structure) {
        for (Map.Entry<Resource, Integer> entry : Constants.STRUCTURE_COSTS.get(structure).entrySet()) {
            if (countResources(entry.getKey()) < entry.getValue()) return false;
        }
        return true;
    }

    public boolean canBuildSomething() {
        for (Structure structure : Constants.ALL_STRUCTURES) {
            if (canBuild(structure)) return true;
        }
        return false;
    }

    public void pay(Structure structure) {
        removeResources(Constants.STRUCTURE_COSTS.get(structure));
    }

    public int getVictoryPoints() {
        return game.getBoard().getStructuresFromPlayer(Structure.VILLAGE, this).size() +
                (game.getBoard().getStructuresFromPlayer(Structure.CITY, this).size() * 2)
                + getLongestRoadScore() + getLargestArmyScore() + amountOfUsedDevelopmentcard(DevelopmentCard.VICTORY_POINT);
    }

    int getLongestRoadScore() {
        if (game.getLongestRoadAward().getPlayer() == this) return 2;
        return 0;
    }

    int getLargestArmyScore() {
        if (game.getLargestArmyAward().getPlayer() == this) return 2;
        return 0;
    }

    public void addDevelopmentCard(DevelopmentCard developmentCard) {
        developmentCards.put(developmentCard, developmentCards.getOrDefault(developmentCard, 0) + 1);
    }

    public boolean useDevelopmentCard(DevelopmentCard developmentCard) {
        if (developmentCards.getOrDefault(developmentCard, 0) == 0) return false;

        developmentCards.put(developmentCard, developmentCards.getOrDefault(developmentCard, 0) - 1);
        usedDevelopmentCards.put(developmentCard, usedDevelopmentCards.getOrDefault(developmentCard, 0) + 1);
        return true;
    }

    public int amountOfUsedDevelopmentcard(DevelopmentCard developmentCard) {
        return usedDevelopmentCards.getOrDefault(developmentCard, 0);
    }

    public int amountOfUnusedDevelopmentCards() {
        AtomicInteger total = new AtomicInteger();
        developmentCards.forEach((developmentCard, amount) -> total.addAndGet(amount));
        return total.get();
    }
}

