public class Bandit {
    private Tile tile;

    public Bandit(Tile tile) {
        this.tile = tile;
    }

    public Tile getTile() {
        return tile;
    }

    public void setTile(Tile tile) {
        this.tile = tile;
    }

    @Override
    public String toString() {
        return "{" +
                "\"model\": \"bandit\", " +
                "\"attributes\": {" +
                "\"tile_key\": \"" + tile.getKey() + "\"" +
                "}" +
                "}";
    }
}
