
public class Node {
    private Tile t;
    private Tile l;
    private Tile r;

    Node(Tile t, Tile r, Tile l) {
        this.t = t;
        this.r = r;
        this.l = l;
    }

    public String getKey() {
        return "(" + t.getKey() + "," + l.getKey() + "," + r.getKey() + ")";
    }
}
