package board;// a tile can either be land or water

import game.Resource;

import java.util.ArrayList;

public class Tile implements Comparable<Tile> {
    private int x;
    private int y;
    private Type type;
    private int number; // the number of the dice to be hit
    private ArrayList<Node> nodes = new ArrayList<>();
    private HarbourType harbour;

    Tile(int x, int y, Type type) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.number = 0;
        this.harbour = HarbourType.HARBOUR_NONE;
    }

    Tile(int x, int y, Type type, int number) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.number = number;
        this.harbour = HarbourType.HARBOUR_NONE;
    }

    // to get the distance between two tiles we need to convert to the cube system and then do the calculation
    // see: https://www.redblobgames.com/grids/hexagons/#distances
    int getDistance(Tile otherTile) {
        return getDistance(this, otherTile);
    }

    int getDistance(Tile tile1, Tile tile2) {
        int x1 = tile1.getX() - (tile1.getY() + (Math.abs(tile1.getY())%2))/2;
        int z1 = tile1.getY();
        int y1 = -x1 - z1;

        int x2 = tile2.getX() - (tile2.getY() + (Math.abs(tile2.getY())%2))/2;
        int z2 = tile2.getY();
        int y2 = -x2 - z2;

        return (Math.abs(x1 - x2) + Math.abs(y1 - y2) + Math.abs(z1 - z2)) / 2;
    }


    // this also contains nodes that are not the same as in the board.getNodes().
    // because there the duplicates are filtered out.
    void addNode(Node n) {
        if (!nodes.contains(n)) {
            nodes.add(n);
        }
    }

    public ArrayList<Node> getNodes() {
        return nodes;
    }

    boolean isEven() {
        return Math.abs(getY())%2 == 0;
    }

    public HarbourType getHarbour() {
        return harbour;
    }

    public void setHarbour(HarbourType harbour) {
        this.harbour = harbour;
    }

    boolean hasHarbour() {
        return this.harbour != HarbourType.HARBOUR_NONE;
    }

    @java.lang.Override
    public java.lang.String toString() {
        return "{" +
                "\"model\": \"tile\", " +
                "\"attributes\": {" +
                "\"key\": \"" + getKey() + "\", " +
                "\"resource_type\": \"" + type.toString() + "\", " +
                "\"harbour_type\": \"" + harbour.toString() + "\", " +
                "\"number\": " + number + ", " +
                "\"x\": " + x + ", " +
                "\"y\": " + y +
                "}" +
                "}";
    }

    public String getKey() {
        return "[" + x + "," + y + "]";
    }

    public Type getType() {
        return type;
    }

    public Resource produces() {
        switch (type) {
            case WOOL: return Resource.WOOL;
            case WOOD: return Resource.WOOD;
            case STONE: return Resource.STONE;
            case GRAIN: return Resource.GRAIN;
            case ORE: return Resource.ORE;
            default: return Resource.NONE;
        }
    }

    public boolean isTerrain() {
        return (type != Type.SEA);
    }

    public Integer getCoordinateSum() {
        return (x*10) + y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getNumber() { return number; }

    @Override
    public int compareTo(Tile tile) {
        return getCoordinateSum().compareTo(tile.getCoordinateSum());
    }
}

enum Type {
    DESERT,
    WOOL,
    WOOD,
    STONE,
    GRAIN,
    ORE,
    SEA,
}

