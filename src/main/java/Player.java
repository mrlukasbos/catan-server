import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

class Player {
    private String name;
    private int id;
    private int lastDiceThrow = 0;
    private ArrayList<Resources> resources = new ArrayList<Resources>();
    private String color;
    private Socket socket;

    Player(int id, String name) {
        this.id = id;
        this.name = name;
        this.color = String.format("#%06x", new Random().nextInt(0xffffff + 1));
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

    String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    // return a random number between 1 and 12
    int throwDice() {
        Random r = new Random();

        // Catan works with two dice of 6 sides, which gives a different distribution than if we would throw one dice of 12 sides
        // (there is only two ways of throwing 2, which is 1+1 and 1+1, but there are multiple number combinations for 7: 1+6, 2+5, 3+4, 4+3, 5+2 and 6+1
        int dice1 = Math.abs(r.nextInt() % 6) + 1;
        int dice2 = Math.abs(r.nextInt() % 6) + 1;
        lastDiceThrow = dice1 + dice2;
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

    @java.lang.Override
    public java.lang.String toString() {
        return "{" +
                "\"model\": \"player\", " +
                "\"id\": " + getId() + ", " +
                "\"attributes\": {" +
                "\"color\": \"" + getColor() + "\", " +
                "\"name\": \"" + getName() + "\"" +
                "}" +
                "}";
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