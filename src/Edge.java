public class Edge {
    private Tile a;
    private Tile b;
    boolean road;

    Edge(Tile a, Tile b) {
        this.a = a;
        this.b = b;
        this.road = false;
    }

    public String getKey() {
        return "(" + a.getKey() + "," + b.getKey() + ")";
    }

    @java.lang.Override
    public java.lang.String toString() {
        return "{" +
                "\"model\": \"edge\", " +
                "\"key\": \"" + getKey() + "\", " +
                "\"attributes\": {" +
                "\"road\": " + road +
                "}" +
                "}";
    }
}
