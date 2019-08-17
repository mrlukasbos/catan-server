// a tile can either be land or water

import java.util.ArrayList;

class Tile implements Comparable<Tile> {
    private int x;
    private int y;
    private Type type;
    private int number; // the number of the dice to be hit
    private Orientation orientation;
    private Board board;

    private ArrayList<Node> nodes = new ArrayList<>();

    Tile(Board board, int x, int y, Type type) {
        this.board = board;
        this.x = x;
        this.y = y;
        this.type = type;
        this.number = 0;
        this.orientation = Orientation.NONE;
    }

    Tile(Board board, int x, int y, Type type, int number) {
        this.board = board;
        this.x = x;
        this.y = y;
        this.type = type;
        this.number = number;
        this.orientation = Orientation.NONE;
    }

    Tile(Board board, int x, int y, Type type, Orientation orientation) {
        this.board = board;
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

    public Resource typeToResource(Type type) {
        switch (type) {
            case WOOL: return Resource.WHOOL;
            case WOOD: return Resource.WOOD;
            case STONE: return Resource.STONE;
            case ORE: return Resource.ORE;
            case GRAIN: return Resource.GRAIN;
            default: return Resource.NONE;
        }
    }

    public String typeToString(Type type) {
        switch (type) {
            case DESERT:
                return "desert";
            case WOOL:
                return "wool";
            case WOOD:
                return "wood";
            case STONE:
                return "stone";
            case GRAIN:
                return "grain";
            case ORE:
                return "ore";
            case SEA:
                return "sea";
            case HARBOUR_WOOL:
                return "harbour_wool";
            case HARBOUR_WOOD:
                return "harbour_wood";
            case HARBOUR_STONE:
                return "harbour_stone";
            case HARBOUR_GRAIN:
                return "harbour_grain";
            case HARBOUR_ORE:
                return "harbour_ore";
            case HARBOUR_ALL:
                return "harbour_all";
            default:
                return "unknown";
        }
    }

    public String orientationToString(Orientation orientation) {
        switch (orientation) {
            case TOP_LEFT:
                return "top_left";
            case TOP_RIGHT:
                return "top_right";
            case LEFT:
                return "left";
            case RIGHT:
                return "right";
            case BOTTOM_LEFT:
                return "bottom_left";
            case BOTTOM_RIGHT:
                return "bottom_right";
            case NONE:
                return "none";
            default:
                return "unknown";
        }
    }

    @java.lang.Override
    public java.lang.String toString() {
        return "{" +
                "\"model\": \"tile\", " +
                "\"key\": \"" + getKey() + "\", " +
                "\"attributes\": {" +
                "\"type\": \"" + typeToString(type) + "\", " +
                "\"number\": " + number + ", " +
                "\"orientation\": \"" + orientationToString(orientation) + "\", " +
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
