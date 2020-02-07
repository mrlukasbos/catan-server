/*
An Edge is the edge between two tiles on the board, and can be a road in the game.
 */
import java.util.Arrays;

public class Edge {
    private Tile a;
    private Tile b;
    private boolean road;
    private Player player;
    private HarbourType harbour;

    Edge(Tile a, Tile b) {
        this.a = a;
        this.b = b;
        this.road = false;
        this.harbour = HarbourType.HARBOUR_NONE;
    }

    public Player getPlayer() {
        return player;
    }

    void setPlayer(Player player) {
        this.player = player;
    }

    boolean hasPlayer() {
        return player != null;
    }

    public boolean isRoad() {
        return road;
    }

    void setRoad(boolean road) {
        this.road = road;
    }

    void setHarbour(HarbourType harbour) {
        this.harbour = harbour;
    }

    boolean isHarbour() {
        return this.harbour != HarbourType.HARBOUR_NONE;
    }

    HarbourType getHarbourType() {
        return this.harbour;
    }

    Tile[] getTiles() {
        Tile[] tiles = {a,b};
        return tiles;
    }

    boolean isOnTerrain() {
        return a.isTerrain() || b.isTerrain();
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
                "\"attributes\": {" +
                "\"key\": \"" + getKey() + "\", " +
                playerString +
                "\"road\": " + road  + ", " +
                "\"harbour\": " + isHarbour() +
                "}" +
                "}";
    }
}
enum HarbourType{
    HARBOUR_NONE,
    HARBOUR_WOOL,
    HARBOUR_WOOD,
    HARBOUR_STONE,
    HARBOUR_GRAIN,
    HARBOUR_ORE,
    HARBOUR_ALL,
}