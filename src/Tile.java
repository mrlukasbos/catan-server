// a tile can either be land

class Tile {
    private Type type;
    private int number; // the number of the dice to be hit
    private Tile[] sides = new Tile[6];

    Tile(Type type) {
        this.type = type;
        this.number = 0;
    }

    Tile(Type type, int number) {
        this.type = type;
        this.number = number;
    }

    void setSides(Tile[] sides) {
        assert sides.length == 6;
        this.sides = sides;
    }

    public String typeToString(Type type) {
        switch (type) {
            case DESERT:
                return "desert";
            case WHOOL:
                return "whool";
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
            case HARBOUR_WHOOL:
                return "harbour_whool";
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
            default:
                return "unknown";
        }
    }

    @java.lang.Override
    public java.lang.String toString() {
        return "{" +
                "\"type\": \"tile\"," +
                "\"attributes\": {" +
                "\"type\": \"" + typeToString(this.type) + "\"," +
                "\"number\": " + number +
                "}" +
                "}";
    }
}

enum Type {
    DESERT,
    WHOOL,
    WOOD,
    STONE,
    GRAIN,
    ORE,
    SEA,
    HARBOUR_WHOOL,
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
    BOTTOM_RIGHT
}
