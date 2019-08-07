
public class Node {
    private Tile t;
    private Tile l;
    private Tile r;
    private Structure structure;

    Node(Tile t, Tile r, Tile l) {
        this.t = t;
        this.r = r;
        this.l = l;
        this.structure = Structure.NONE;
    }

    public String getKey() {
        return "(" + t.getKey() + "," + l.getKey() + "," + r.getKey() + ")";
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
        return "{" +
                "\"model\": \"node\", " +
                "\"key\": \"" + getKey() + "\", " +
                "\"attributes\": {" +
                "\"structure\": \"" + structureToString(structure) + "\"" +
                "}" +
                "}";
    }
}

enum Structure {
    NONE,
    SETTLEMENT,
    CITY
}