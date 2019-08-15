/*
A node is a junction between tiles. It can support settlements and cities in the game
 */

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
        this.structure = Structure.NONE;
    }

    double getDistanceToNode(Node otherNode) {
        // pick a surrounding node. We don't care which one as long as it's the same relative to the node.
        // this will be the node with the smalles coordinate value.
        // this node is either at the left-top or at the exact top of the node

        // if for both nodes the relative position of their key-tile is the same, then
        // the distance is the distance between tiles * 2
        // otherwise we have to subtract 1 from the total distance
        // we make the distance absolute for the case where two nodes have the same key tile

        Tile a = getSortedNeighboursTiles()[0];
        Tile b = otherNode.getSortedNeighboursTiles()[0];

        // is the key tile right above the node? We will know by seeing if the y values of the other nodes are then different.
        boolean tileOnTopforThis = (a.getY() != getSortedNeighboursTiles()[1].getY() && a.getY() != getSortedNeighboursTiles()[2].getY());
        boolean tileOnTopforOther = (b.getY() != otherNode.getSortedNeighboursTiles()[1].getY() && b.getY() != otherNode.getSortedNeighboursTiles()[2].getY());
        int dist = (a.getDistance(b) * 2);
        if (tileOnTopforThis ^ tileOnTopforOther) { // xor
            dist -= 1;
        }

        return Math.abs(dist);
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

    boolean hasStructure() {
        return structure != Structure.NONE;
    }

    public Structure getStructure() {
        return structure;
    }

    void setStructure(Structure structure) {
        this.structure = structure;
    }

    Tile[] getTiles() {
        Tile[] tiles = {t, l, r};
        return tiles;
    }

    Tile[] getSortedNeighboursTiles() {
        Tile[] neighbors = {t, l, r};
        Arrays.sort(neighbors);
        return neighbors;
    }

    String getKey() {
        Tile[] neighbors = {t, l, r};
        Arrays.sort(neighbors);
        return "(" + neighbors[0].getKey() + "," + neighbors[1].getKey() + "," + neighbors[2].getKey() + ")";
    }

    String structureToString(Structure structure) {
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
    CITY,
    STREET
}