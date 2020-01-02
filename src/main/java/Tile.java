// a tile can either be land or water

import java.util.ArrayList;

class Tile implements Comparable<Tile> {
    private int x;
    private int y;
    private Type type;
    private int number; // the number of the dice to be hit
    private Orientation orientation;

    private ArrayList<Node> nodes = new ArrayList<>();

    Tile(int x, int y, Type type) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.number = 0;
        this.orientation = Orientation.NONE;
    }

    Tile(int x, int y, Type type, int number) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.number = number;
        this.orientation = Orientation.NONE;
    }

    Tile(int x, int y, Type type, Orientation orientation) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.number = 0;
        this.orientation = orientation;
    }

    // to get the distance between two tiles we need to convert to the cube system and then do the calculation
    // see: https://www.redblobgames.com/grids/hexagons/#distances
    int getDistance(Tile otherTile) {
        Cube a = new Cube(this);
        Cube b = new Cube(otherTile);

        return a.getDistance(b);

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

    @java.lang.Override
    public java.lang.String toString() {
        return "{" +
                "\"model\": \"tile\", " +
                "\"attributes\": {" +
                "\"key\": \"" + getKey() + "\", " +
                "\"resource_type\": \"" + type.toString() + "\", " +
                "\"number\": " + number + ", " +
                "\"orientation\": \"" + orientation.toString() + "\", " +
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
        return (orientation == Orientation.NONE && type != Type.SEA);
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
    HARBOUR_WOOL,
    HARBOUR_WOOD,
    HARBOUR_STONE,
    HARBOUR_GRAIN,
    HARBOUR_ORE,
    HARBOUR_ALL
}

// used for harbours
enum Orientation {
    TOP_LEFT,
    TOP_RIGHT,
    LEFT,
    RIGHT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT,
    NONE
}
