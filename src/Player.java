import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

class Player {
    private String name;
    private int id;
    private int lastDiceThrow = 0;
    private ArrayList<Resources> resources = new ArrayList<Resources>();

    public Socket getSocket() {
        return socket;
    }

    private Socket socket;

    Player(int id, String name) {
        this.id = id;
        this.name = name;
    }

    void setSocket(Socket s) {
        socket = s;
    }

    String getName() {
        return name;
    }

    // return a random number between 1 and 12
    int throwDice() {
        Random r = new Random();
        lastDiceThrow = Math.abs(r.nextInt() % 12) + 1;
        return lastDiceThrow;
    }

    int getLastDiceThrow() {
        return lastDiceThrow;
    }

    int getId() {
        return id;
    }

    // count the occurence of a specific resource for the player
    int countResources(Resources resourceToCount) {
        int count = 0;
        for (Resources resourceOnHand : resources) {
            if (resourceOnHand == resourceToCount) {
                count++;
            }
        }
        return count;
    }

    // add resources to the resources of the player
    void addResources(Resources resource, int amount) {
        for (int i = 0; i < amount; i++) {
            resources.add(resource);
        }
    }

    // remove resources from the player
    void removeResources(Resources resource, int amount) {
        // iterate over all resources to remove the amount needed.
        int amountOfRemovals = 0;
        for (int i = 0; i < resources.size(); i++) {
            if (resources.get(i) == resource && amountOfRemovals < amount) {
                resources.remove(i);
                amountOfRemovals++;
            }
        }
    }
}

enum Resources {
    DESERT,
    WHOOL,
    WOOD,
    STONE,
    GRAIN,
    ORE
}