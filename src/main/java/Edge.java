public class Edge {
    private Tile a;
    private Tile b;

    public boolean isRoad() {
        return road;
    }

    public void setRoad(boolean road) {
        this.road = road;
    }

    private boolean road;
    private Player player;

    Edge(Tile a, Tile b) {
        this.a = a;
        this.b = b;
        this.road = false;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public boolean isRoad() {
        return road;
    }

    public void setRoad(boolean road) {
        this.road = road;
    }

    public String getKey() {
        return "(" + a.getKey() + "," + b.getKey() + ")";
    }

    @java.lang.Override
    public java.lang.String toString() {
        String playerString = "";
        if (player != null) {
            playerString = "\"player\": " + player.getId() + "," +
                    "\"player_color\": \"" + player.getColor() + "\",";
        }
        return "{" +
                "\"model\": \"edge\", " +
                "\"key\": \"" + getKey() + "\", " +
                "\"attributes\": {" +
                playerString +
                "\"road\": " + road +
                "}" +
                "}";
    }
}
