import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

class Player {
    private String name;
    private int id;
    private HashMap<Resource, Integer> resources = new HashMap<>();
    private String color;
    private Socket socket;

    Player(int id, String name) {
        this.id = id;
        this.name = name;
        this.color = String.format("#%06x", new Random().nextInt(0xffffff + 1));

        resources.put(Resource.GRAIN, 0);
        resources.put(Resource.ORE, 0);
        resources.put(Resource.STONE, 0);
        resources.put(Resource.WHOOL, 0);
        resources.put(Resource.WOOD, 0);
    }

    synchronized void setSocket(Socket s) {
        socket = s;
    }

    synchronized Socket getSocket() {
        return socket;
    }

    synchronized void send(String str) {
        try {
            DataOutputStream out = new DataOutputStream(getSocket().getOutputStream());
            out.writeUTF(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // blocking implementation of reading
    synchronized String listen() {
        try {
            InputStream inputStream = getSocket().getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            // nonblocking implementation
            // if (inputStream.available() != 0 && reader.ready()) {
            //     return reader.readLine();
            // }

            // blocking implementation
            return reader.readLine();

        } catch (IOException e) {
            e.printStackTrace();
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
        return resources.size();
    }

    // add resources to the resources of the player
    void addResources(Resource resource, int amount) {
        if (resource != Resource.NONE) {
            resources.replace(resource, resources.get(resource) + amount);
        }
    }

    // remove resources from the player
    void removeResources(Resource resource, int amount) {
        // iterate over all resources to remove the amount needed.
        int amountOfResources = countResources(resource);
        resources.replace(resource, Math.max(0, amountOfResources - amount));
    }

    @java.lang.Override
    public java.lang.String toString() {

        String resourcesString = getResourcesAsJSONString(resources);

        return "{" +
                "\"model\": \"player\", " +
                "\"id\": " + getId() + ", " +
                "\"attributes\": {" +
                "\"color\": \"" + getColor() + "\", " +
                "\"name\": \"" + getName() + "\", " +
                "\"resources\": " + resourcesString +
                "}" +
                "}";
    }

    static String getResourcesAsJSONString(HashMap<Resource, Integer> resources) {
        String resourcesString = "[";
        if (resources.size() == 0) {
            resourcesString = "[]";
        } else {
            for (HashMap.Entry<Resource, Integer> entry : resources.entrySet()) {
                resourcesString = resourcesString.concat("{\"type\":\"" + resourceToString(entry.getKey()) + "\", \"value\":" + entry.getValue()) + "},";
            }
            resourcesString = resourcesString.substring(0, resourcesString.length() - 1);
            resourcesString = resourcesString.concat("]");
        }
        return resourcesString;
    }

    private void print(String msg) {
        System.out.println("[Player] " + msg);
    }

    static String resourceToString(Resource res) {
        switch (res) {
            case WHOOL: return "whool";
            case WOOD: return "wood";
            case STONE: return "stone";
            case GRAIN: return "grain";
            case ORE: return "ore";
            default: return null;
        }
    }

    static Resource stringToResource(String str) {
        switch (str) {
            case "whool": return Resource.WHOOL;
            case "wood": return Resource.WOOD;
            case "stone": return Resource.STONE;
            case "grain": return Resource.GRAIN;
            case "ore": return Resource.ORE;
            default: return Resource.NONE;
        }
    }

    static String structureToString(Structure structure) {
        switch (structure) {
            case STREET:
                return "street";
            case SETTLEMENT:
                return "village";
            case CITY:
                return "city";
            default:
                return "";
        }
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
            if (!canBuild(structure)) return false;
        }
        return true;
    }

    void pay(Structure structure) {
        for (Map.Entry<Resource, Integer> entry : Constants.STRUCTURE_COSTS.get(structure).entrySet()) {
            removeResources(entry.getKey(), entry.getValue());
        }
    }
}

enum Resource {
    NONE,
    WHOOL,
    WOOD,
    STONE,
    GRAIN,
    ORE
}