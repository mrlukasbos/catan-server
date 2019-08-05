public class Edge {
    private Tile a;
    private Tile b;

    Edge(Tile a, Tile b) {
        this.a = a;
        this.b = b;
    }

    public String getKey() {
        return "(" + a.getKey() + "," + b.getKey() + ")";
    }
}
