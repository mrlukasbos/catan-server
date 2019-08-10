import java.util.Arrays;

public class Node {
    private Tile t;
    private Tile l;
    private Tile r;

    public Structure getStructure() {
        return structure;
    }

    public void setStructure(Structure structure) {
        this.structure = structure;
    }

    private Structure structure;
    private Player player;

    Node(Tile t, Tile r, Tile l) {
        this.t = t;
        this.r = r;
        this.l = l;
        this.structure = Structure.NONE;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public String getKey() {
        Tile[] neighbors = {t, l, r};
        Arrays.sort(neighbors);
        return "(" + neighbors[0].getKey() + "," + neighbors[1].getKey() + "," + neighbors[2].getKey() + ")";
    }

    public String structureToString(Structure structure) {
        switch (structure) {
            case NONE:
                return "none";
            case CITY:
                return "city";
            case SETTLEMENT:
                return "settlement";
            default:
                return "unknown";
        }
    }

    @java.lang.Override
    public java.lang.String toString() {
        String playerString = "";
        if (player != null) {
            playerString = "\"player\": " + player.getId() + "," +
                    "\"player_color\": \"" + player.getColor() + "\",";
        }
        return "{" +
                "\"model\": \"node\", " +
                "\"key\": \"" + getKey() + "\", " +
                "\"attributes\": {" +
                "\"structure\": \"" + structureToString(structure) + "\"," +
                playerString +
                "\"t_key\": \"" + t.getKey() + "\"," +
                "\"r_key\": \"" + r.getKey()+ "\"," +
                "\"l_key\": \"" + l.getKey() + "\"" +
                "}" +
                "}";
    }
}

enum Structure {
    NONE,
    SETTLEMENT,
    CITY
}