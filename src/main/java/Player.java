import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

class Player {
    private String name;
    private int id;
    private HashMap<Resource, Integer> resources = new HashMap<>();
    private HashMap<DevelopmentCard, Integer> developmentCards = new HashMap<>();
    private String color;
    private Socket socket;
    private Game game;

    Player(Game game, int id, String name) {
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

    synchronized void setSocket(Socket s) {
        socket = s;
    }

    synchronized Socket getSocket() {
        return socket;
    }

    synchronized void send(String str) {
        if (getSocket() != null) {
            try {
                str += "\r\n";
                BufferedOutputStream bos = new BufferedOutputStream(getSocket().getOutputStream());
                bos.write(str.getBytes("UTF-8"));
                bos.flush();
            } catch (IOException e) {
               // System.exit(1);
                e.printStackTrace();
            }
        }
    }

    // blocking implementation of reading
    synchronized String listen() {
        if (getSocket() != null) {
            try {
                BufferedInputStream bf = new BufferedInputStream(getSocket().getInputStream());
                BufferedReader r = new BufferedReader(new InputStreamReader(bf, StandardCharsets.UTF_8));
                return r.readLine();

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public HashMap<Resource, Integer> getResources() {
        return resources;
    }

    String getName() {
        return name;
    }

    String getColor() {
        return color;
    }

    int getId() {
        return id;
    }

    // count the occurence of a specific resource for the player
    int countResources(Resource resourceToCount) {
        return resources.getOrDefault(resourceToCount, 0);
    }

    // count the occurence of all resources together
    int countResources() {
        int count = 0;
        for (Resource res : Constants.ALL_RESOURCES) {
            count += countResources(res);
        }
        return count;
    }

    // add resources to the resources of the player
    void addResources(Resource resource, int amount) {
        if (resource != Resource.NONE) {
            resources.replace(resource, Math.max(0, resources.get(resource) + amount));
        }
    }

    void addResources(Map<Resource, Integer> newResources) {
        for (Map.Entry<Resource, Integer> entry : newResources.entrySet()) {
            addResources(entry.getKey(), entry.getValue());
        }
    }

    void removeResources(Resource resource, int amount) {
        addResources(resource, -amount);
    }

    void removeResources(Map<Resource, Integer> resourcesToRemove) {
        for (Map.Entry<Resource, Integer> entry : resourcesToRemove.entrySet()) {
            removeResources(entry.getKey(), entry.getValue());
        }
    }

    void removeResources() {
        for (Resource resource : Constants.ALL_RESOURCES) {
            resources.replace(resource, 0);
        }
    }

    @java.lang.Override
    public java.lang.String toString() {
        String resourcesString = Helpers.getJSONArrayFromHashMap(resources, "type", "value");
        String developmentCardsString = Helpers.getJSONArrayFromHashMap(developmentCards, "type", "value");

        return "{" +
                "\"model\": \"player\", " +
                "\"id\": " + getId() + ", " +
                "\"attributes\": {" +
                "\"color\": \"" + getColor() + "\", " +
                "\"name\": \"" + getName() + "\", " +
                "\"resources\": " + resourcesString + ", " +
                "\"development_cards\": " + developmentCardsString +

                "}" +
                "}";
    }

    private void print(String msg) {
        System.out.println("[Player] " + msg);
    }

    boolean canTradeWithBank() {
        for (Map.Entry<Resource, Integer> resource : resources.entrySet()) {
            if (resource.getValue() >= Constants.MINIMUM_CARDS_FOR_TRADE) return true;
        }
        return false;
    }

    boolean canBuild(Structure structure) {
        for (Map.Entry<Resource, Integer> entry : Constants.STRUCTURE_COSTS.get(structure).entrySet()) {
            if (countResources(entry.getKey()) < entry.getValue()) return false;
        }
        return true;
    }

    boolean canBuildSomething() {
        for (Structure structure : Constants.ALL_STRUCTURES) {
            if (canBuild(structure)) return true;
        }
        return false;
    }

    void pay(Structure structure) {
        removeResources(Constants.STRUCTURE_COSTS.get(structure));
    }

    int getVisibleVictoryPoints() {
        return game.getBoard().getStructuresFromPlayer(Structure.CITY, this).size() +
                game.getBoard().getStructuresFromPlayer(Structure.VILLAGE, this).size();
    }

    int getAllVictoryPoints() {
        return getVisibleVictoryPoints() + developmentCards.get(DevelopmentCard.VICTORY_POINT);
    }

    void addDevelopmentCard() {
        int randomIndex = new Random().nextInt() % Constants.ALL_DEVELOPMENT_CARDS.length;
        DevelopmentCard newCard = Constants.ALL_DEVELOPMENT_CARDS[randomIndex];
        developmentCards.put(newCard, developmentCards.getOrDefault(newCard, 0) + 1);
    }

    void removeDevelopmentCard(DevelopmentCard card) {
        developmentCards.put(card, Math.max(0, developmentCards.getOrDefault(card, 0) - 1));
    }



}

enum Resource {
    NONE,
    WOOL,
    WOOD,
    STONE,
    GRAIN,
    ORE
}