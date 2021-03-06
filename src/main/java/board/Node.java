package board;/*
A node is a junction between tiles. It can support settlements and cities in the game
 */

import game.Player;

import java.util.Arrays;

public class Node {
    private Tile t;
    private Tile l;
    private Tile r;
    private Structure structure;
    private Player player;

    Node(Tile t, Tile r, Tile l) {
        this.t = t;
        this.r = r;
        this.l = l;

        // we can also let the tile know which nodes are connected to it
        t.addNode(this);
        r.addNode(this);
        l.addNode(this);
        this.structure = Structure.NONE;
    }

    public Player getPlayer() {
        return player;
    }

    void setPlayer(Player player) {
        this.player = player;
    }

    public boolean hasPlayer() {
        return player != null;
    }

    public boolean hasStructure() {
        return structure != Structure.NONE;
    }

    public Structure getStructure() {
        return structure;
    }

    void setStructure(Structure structure) {
        this.structure = structure;
    }

    Tile[] getTiles() {
        return new Tile[]{t, l, r};
    }

    Tile[] getSortedNeighboursTiles() {
        Tile[] neighbors = {t, l, r};
        Arrays.sort(neighbors);
        return neighbors;
    }

    public String getKey() {
        Tile[] neighbors = {t, l, r};
        Arrays.sort(neighbors);
        return "(" + neighbors[0].getKey() + "," + neighbors[1].getKey() + "," + neighbors[2].getKey() + ")";
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
                "\"attributes\": {" +
                "\"key\": \"" + getKey() + "\", " +
                "\"structure\": \"" + structure.toString() + "\"," +
                playerString +
                "\"t_key\": \"" + t.getKey() + "\"," +
                "\"r_key\": \"" + r.getKey()+ "\"," +
                "\"l_key\": \"" + l.getKey() + "\"" +
                "}" +
                "}";
    }
}

