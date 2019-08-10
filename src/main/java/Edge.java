/*
An Edge is the edge between two tiles on the board, and can be a road in the game.
 */
import java.util.Arrays;

public class Edge {
    private Tile a;
    private Tile b;
    private boolean road;
    private Player player;

    Edge(Tile a, Tile b) {
        this.a = a;
        this.b = b;
        this.road = false;
    }

    public Player getPlayer() {
        return player;
    }

    void setPlayer(Player player) {
        this.player = player;
    }

    public boolean isRoad() {
        return road;
    }

    void setRoad(boolean road) {
        this.road = road;
    }

    String getKey() {
        Tile[] neighbors = {a, b};
        Arrays.sort(neighbors);
        return "(" + neighbors[0].getKey() + "," + neighbors[1].getKey() + ")";
    }

    @java.lang.Override
    public java.lang.String toString() {
        String playerString = "";
        if (player != null) {
            playerString = "\"player\": " + player.getId() + "," +
                    "\"player_color\": \"" + player.getColor() + "\",";
        }
        return "{" +
                "\"model\": \"edge\", " +
                "\"key\": \"" + getKey() + "\", " +
                "\"attributes\": {" +
                playerString +
                "\"road\": " + road +
                "}" +
                "}";
    }
}
